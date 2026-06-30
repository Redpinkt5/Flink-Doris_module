package cn.rp.ai;

import cn.rp.ai.rag.QAExample;
import cn.rp.ai.rag.RagRepository;
import cn.rp.ai.rag.RagRetriever;
import cn.rp.ai.semantic.MetricMatchResult;
import cn.rp.ai.semantic.MetricSemanticParser;
import cn.rp.config.AiSchemaConfig;
import cn.rp.util.DorisDBUtil;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.*;

/**
 * AI 问答服务核心
 * 编排：Prompt → DeepSeek LLM → SQL → Doris → 结果转换 + 图表配置
 */
@Service
public class AiService {

    private static final Logger logger = LoggerFactory.getLogger(AiService.class);

    @Value("${deepseek.api.url:https://api.deepseek.com/v1/chat/completions}")
    private String deepseekUrl;

    @Value("${deepseek.api.key:}")
    private String deepseekApiKey;

    @Value("${deepseek.model:deepseek-chat}")
    private String modelName;

    private final OkHttpClient httpClient = new OkHttpClient.Builder()
            .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
            .writeTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .build();

    private final Gson gson = new Gson();

    @Autowired
    private AiPromptBuilder promptBuilder;

    @Autowired
    private AiSqlValidator sqlValidator;

    @Autowired
    private AiSchemaConfig schemaConfig;

    @Autowired
    private MetricSemanticParser metricSemanticParser;

    @Autowired
    private RagRetriever ragRetriever;

    @Autowired
    private RagRepository ragRepository;

    @Autowired
    @Qualifier("aiHistoryTaskExecutor")
    private TaskExecutor historyTaskExecutor;

    /**
     * 处理用户自然语言查询
     */
    public AiQueryResult ask(String question) {
        if (question == null || question.trim().isEmpty()) {
            return AiQueryResult.fail("问题不能为空");
        }

        try {
            // 1. 语义解析与 RAG 检索
            List<MetricMatchResult> matches = metricSemanticParser != null
                    ? metricSemanticParser.parse(question)
                    : Collections.emptyList();
            List<QAExample> examples = ragRetriever != null
                    ? ragRetriever.retrieve(question, 3)
                    : Collections.emptyList();

            // 2. 构建 Prompt
            String systemPrompt = promptBuilder.buildSystemPrompt(matches, examples);
            String userPrompt = promptBuilder.buildUserPrompt(question);

            // 2. 调用 DeepSeek
            LlmResponse llmResponse = callDeepSeek(systemPrompt, userPrompt);
            if (llmResponse == null || llmResponse.sql == null || llmResponse.sql.trim().isEmpty()) {
                return AiQueryResult.fail("AI 未能生成有效 SQL，请换个问法");
            }

            // 3. 清理 SQL
            String sql = llmResponse.sql.trim().replaceAll("^```sql\\s*", "")
                    .replaceAll("^```\\s*", "").replaceAll("\\s*```$", "").trim();

            // 4. 校验 SQL
            AiSqlValidator.ValidationResult validation = sqlValidator.validate(sql);
            if (!validation.isValid()) {
                logger.warn("SQL validation failed: {} | SQL: {}", validation.getErrorMessage(), sql);
                return AiQueryResult.fail("生成的 SQL 不符合安全规范: " + validation.getErrorMessage());
            }

            // 5. 执行 SQL
            logger.info("Executing SQL: {}", sql);
            QueryResult queryResult = executeSql(sql);

            // 6. 构建返回结果（含图表配置）
            AiQueryResult result = new AiQueryResult();
            result.setSuccess(true);
            result.setSql(sql);
            result.setColumns(queryResult.columns);
            result.setRows(queryResult.rows);
            result.setRowCount(queryResult.rows.size());
            result.setChartType(llmResponse.chartType);
            result.setChartTitle(llmResponse.title);
            result.setxAxisField(llmResponse.xAxis);
            result.setyAxisField(llmResponse.yAxis);
            result.setxAxisLabel(llmResponse.xAxisLabel);
            result.setyAxisLabel(llmResponse.yAxisLabel);

            // 异步写入 RAG 历史库
            saveToHistory(question, result, matches);

            return result;

        } catch (Exception e) {
            logger.error("AI query failed", e);
            return AiQueryResult.fail("查询失败: " + e.getMessage());
        }
    }

    private void saveToHistory(String question, AiQueryResult result, List<MetricMatchResult> matches) {
        if (!result.isSuccess() || ragRepository == null) {
            return;
        }
        try {
            QAExample example = new QAExample();
            example.setQuestion(question);
            example.setGeneratedSql(result.getSql());
            example.setChartType(result.getChartType());
            example.setxAxisField(result.getxAxisField());
            example.setyAxisField(result.getyAxisField());
            example.setIsSuccess(1);

            if (matches != null && !matches.isEmpty()) {
                example.setMatchedMetrics(matches.get(0).getMetric().getName());
            }

            historyTaskExecutor.execute(() -> ragRepository.save(example));
        } catch (Exception e) {
            logger.warn("Failed to save QA history", e);
        }
    }

    /**
     * 调用 DeepSeek LLM，解析 JSON 结构化返回
     */
    private LlmResponse callDeepSeek(String systemPrompt, String userPrompt) throws Exception {
        JsonObject messageSystem = new JsonObject();
        messageSystem.addProperty("role", "system");
        messageSystem.addProperty("content", systemPrompt);

        JsonObject messageUser = new JsonObject();
        messageUser.addProperty("role", "user");
        messageUser.addProperty("content", userPrompt);

        JsonArray messages = new JsonArray();
        messages.add(messageSystem);
        messages.add(messageUser);

        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("model", modelName);
        requestBody.add("messages", messages);
        requestBody.addProperty("temperature", 0.1);
        requestBody.addProperty("max_tokens", 800);

        RequestBody body = RequestBody.create(
                gson.toJson(requestBody),
                MediaType.parse("application/json; charset=utf-8")
        );

        Request request = new Request.Builder()
                .url(deepseekUrl)
                .addHeader("Authorization", "Bearer " + deepseekApiKey)
                .addHeader("Content-Type", "application/json")
                .post(body)
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new RuntimeException("DeepSeek API error: " + response.code() + " - " + response.message());
            }

            String responseBody = response.body() != null ? response.body().string() : "";
            JsonObject responseJson = gson.fromJson(responseBody, JsonObject.class);

            JsonArray choices = responseJson.getAsJsonArray("choices");
            if (choices == null || choices.size() == 0) {
                if (responseJson.has("error")) {
                    throw new RuntimeException("DeepSeek error: " + responseJson.get("error"));
                }
                return null;
            }

            JsonObject choice = choices.get(0).getAsJsonObject();
            JsonObject message = choice.getAsJsonObject().getAsJsonObject("message");
            String content = message.get("content").getAsString().trim();

            logger.info("LLM raw response:\n{}", content);

            // 尝试解析 JSON 返回
            return parseLlmResponse(content);

        }
    }

    /**
     * 从 LLM 返回内容中解析 JSON（处理 markdown 代码块）
     */
    private LlmResponse parseLlmResponse(String content) {
        String jsonStr = content;
        // 去除 markdown 代码块
        jsonStr = jsonStr.replaceAll("^```json\\s*", "").replaceAll("^```\\s*", "").replaceAll("\\s*```$", "").trim();

        try {
            JsonObject obj = gson.fromJson(jsonStr, JsonObject.class);
            LlmResponse resp = new LlmResponse();
            resp.sql = obj.has("sql") ? obj.get("sql").getAsString() : "";
            resp.chartType = obj.has("chartType") ? obj.get("chartType").getAsString() : "table";
            resp.title = obj.has("title") ? obj.get("title").getAsString() : "";
            resp.xAxis = obj.has("xAxis") ? obj.get("xAxis").getAsString() : "";
            resp.yAxis = obj.has("yAxis") ? obj.get("yAxis").getAsString() : "";
            resp.xAxisLabel = obj.has("xAxisLabel") ? obj.get("xAxisLabel").getAsString() : "";
            resp.yAxisLabel = obj.has("yAxisLabel") ? obj.get("yAxisLabel").getAsString() : "";
            return resp;
        } catch (Exception e) {
            logger.warn("Failed to parse LLM response as JSON, falling back to plain text: {}", e.getMessage());
            // 降级：把整段内容当 SQL，图表默认 table
            LlmResponse resp = new LlmResponse();
            resp.sql = jsonStr;
            resp.chartType = "table";
            return resp;
        }
    }

    /**
     * 执行 SQL 并转换结果
     */
    private QueryResult executeSql(String sql) {
        QueryResult result = new QueryResult();
        result.columns = new ArrayList<>();
        result.rows = new ArrayList<>();

        try {
            String jdbcUrl = schemaConfig.getJdbcUrl();
            String driver = schemaConfig.getJdbcDriver();

            ResultSet rs = DorisDBUtil.quereBySql(jdbcUrl, driver, "hadoop", "hadoop", sql);
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();

            for (int i = 1; i <= columnCount; i++) {
                result.columns.add(metaData.getColumnLabel(i));
            }

            while (rs.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                for (int i = 1; i <= columnCount; i++) {
                    String columnName = metaData.getColumnLabel(i);
                    Object value = rs.getObject(i);
                    row.put(columnName, value);
                }
                result.rows.add(row);
            }

            rs.close();
        } catch (Exception e) {
            logger.error("SQL execution failed: {}", sql, e);
            throw new RuntimeException("SQL 执行失败: " + e.getMessage());
        }

        return result;
    }

    // ==================== 内部类 ====================

    public static class AiQueryResult {
        private boolean success;
        private String sql;
        private String errorMessage;
        private List<String> columns;
        private List<Map<String, Object>> rows;
        private int rowCount;
        // 图表配置
        private String chartType;
        private String chartTitle;
        private String xAxisField;
        private String yAxisField;
        private String xAxisLabel;
        private String yAxisLabel;

        public static AiQueryResult fail(String message) {
            AiQueryResult r = new AiQueryResult();
            r.success = false;
            r.errorMessage = message;
            return r;
        }

        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        public String getSql() { return sql; }
        public void setSql(String sql) { this.sql = sql; }
        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
        public List<String> getColumns() { return columns; }
        public void setColumns(List<String> columns) { this.columns = columns; }
        public List<Map<String, Object>> getRows() { return rows; }
        public void setRows(List<Map<String, Object>> rows) { this.rows = rows; }
        public int getRowCount() { return rowCount; }
        public void setRowCount(int rowCount) { this.rowCount = rowCount; }
        public String getChartType() { return chartType; }
        public void setChartType(String chartType) { this.chartType = chartType; }
        public String getChartTitle() { return chartTitle; }
        public void setChartTitle(String chartTitle) { this.chartTitle = chartTitle; }
        public String getxAxisField() { return xAxisField; }
        public void setxAxisField(String xAxisField) { this.xAxisField = xAxisField; }
        public String getyAxisField() { return yAxisField; }
        public void setyAxisField(String yAxisField) { this.yAxisField = yAxisField; }
        public String getxAxisLabel() { return xAxisLabel; }
        public void setxAxisLabel(String xAxisLabel) { this.xAxisLabel = xAxisLabel; }
        public String getyAxisLabel() { return yAxisLabel; }
        public void setyAxisLabel(String yAxisLabel) { this.yAxisLabel = yAxisLabel; }
    }

    private static class QueryResult {
        List<String> columns;
        List<Map<String, Object>> rows;
    }

    private static class LlmResponse {
        String sql;
        String chartType;
        String title;
        String xAxis;
        String yAxis;
        String xAxisLabel;
        String yAxisLabel;
    }
}
