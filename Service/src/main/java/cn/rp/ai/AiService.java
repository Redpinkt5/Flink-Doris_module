package cn.rp.ai;

import cn.rp.config.AiSchemaConfig;
import cn.rp.util.DorisDBUtil;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.*;

/**
 * AI 问答服务核心
 * 编排：Prompt → DeepSeek LLM → SQL → Doris → 结果转换
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

    /**
     * 处理用户自然语言查询
     * @param question 用户问题
     * @return AI 查询结果
     */
    public AiQueryResult ask(String question) {
        if (question == null || question.trim().isEmpty()) {
            return AiQueryResult.fail("问题不能为空");
        }

        try {
            // 1. 构建 Prompt
            String systemPrompt = promptBuilder.buildSystemPrompt();
            String userPrompt = promptBuilder.buildUserPrompt(question);

            // 2. 调用 DeepSeek
            String sql = callDeepSeek(systemPrompt, userPrompt);
            if (sql == null || sql.trim().isEmpty()) {
                return AiQueryResult.fail("AI 未能生成有效 SQL，请换个问法");
            }

            // 3. 清理 SQL（去除 markdown 代码块标记）
            sql = cleanSql(sql);

            // 4. 校验 SQL
            var validation = sqlValidator.validate(sql);
            if (!validation.isValid()) {
                logger.warn("SQL validation failed: {} | SQL: {}", validation.getErrorMessage(), sql);
                return AiQueryResult.fail("生成的 SQL 不符合安全规范: " + validation.getErrorMessage());
            }

            // 5. 执行 SQL
            logger.info("Executing SQL: {}", sql);
            QueryResult queryResult = executeSql(sql);

            // 6. 构建返回结果
            AiQueryResult result = new AiQueryResult();
            result.setSuccess(true);
            result.setSql(sql);
            result.setColumns(queryResult.columns);
            result.setRows(queryResult.rows);
            result.setRowCount(queryResult.rows.size());

            return result;

        } catch (Exception e) {
            logger.error("AI query failed", e);
            return AiQueryResult.fail("查询失败: " + e.getMessage());
        }
    }

    /**
     * 调用 DeepSeek LLM
     */
    private String callDeepSeek(String systemPrompt, String userPrompt) throws Exception {
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
        requestBody.addProperty("temperature", 0.1);  // 低温度，保证 SQL 稳定性
        requestBody.addProperty("max_tokens", 500);

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

            // 解析 DeepSeek 返回
            JsonArray choices = responseJson.getAsJsonArray("choices");
            if (choices != null && choices.size() > 0) {
                JsonObject choice = choices.get(0).getAsJsonObject();
                JsonObject message = choice.getAsJsonObject().getAsJsonObject("message");
                return message.get("content").getAsString().trim();
            }

            // 检查错误
            if (responseJson.has("error")) {
                throw new RuntimeException("DeepSeek error: " + responseJson.get("error"));
            }

            return null;
        }
    }

    /**
     * 清理 SQL 中的 markdown 代码块标记
     */
    private String cleanSql(String sql) {
        sql = sql.trim();
        // 去除 ```sql 或 ``` 标记
        sql = sql.replaceAll("^```sql\\s*", "");
        sql = sql.replaceAll("^```\\s*", "");
        sql = sql.replaceAll("\\s*```$", "");
        return sql.trim();
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

            // 获取列名
            for (int i = 1; i <= columnCount; i++) {
                result.columns.add(metaData.getColumnLabel(i));
            }

            // 获取数据行
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
    }

    private static class QueryResult {
        List<String> columns;
        List<Map<String, Object>> rows;
    }
}
