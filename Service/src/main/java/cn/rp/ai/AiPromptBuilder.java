package cn.rp.ai;

import cn.rp.ai.rag.QAExample;
import cn.rp.ai.rag.RagRetriever;
import cn.rp.ai.semantic.MetricMatchResult;
import cn.rp.ai.semantic.MetricSemanticParser;
import cn.rp.config.AiSchemaConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

/**
 * AI Prompt 构建器
 * 加载 prompt_template.txt 并注入 schema 内容及动态日期
 */
@Component
public class AiPromptBuilder {

    private static final DateTimeFormatter DAY_FMT = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final int DEFAULT_TOP_K = 3;

    @Autowired
    private AiSchemaConfig schemaConfig;

    @Autowired
    private MetricSemanticParser metricSemanticParser;

    @Autowired
    private RagRetriever ragRetriever;

    private String systemPromptTemplate;

    public String buildSystemPrompt() {
        if (systemPromptTemplate == null) {
            systemPromptTemplate = loadTemplate();
        }
        String schemaContent = schemaConfig.getTableSchemaForPrompt();

        LocalDate today = LocalDate.now();
        String currentDate = today.format(DAY_FMT);
        String yesterday = today.minusDays(1).format(DAY_FMT);

        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter HOUR_FMT = DateTimeFormatter.ofPattern("yyyyMMddHH");
        String currentHour = now.format(HOUR_FMT);
        String hourAgo = now.minusHours(10).format(HOUR_FMT);

        return systemPromptTemplate
                .replace("{schema_content}", schemaContent)
                .replace("{current_date}", currentDate)
                .replace("{yesterday}", yesterday)
                .replace("{date_7days_ago}", today.minusDays(7).format(DAY_FMT))
                .replace("{date_30days_ago}", today.minusDays(30).format(DAY_FMT))
                .replace("{month_start}", today.withDayOfMonth(1).format(DAY_FMT))
                .replace("{current_hour}", currentHour)
                .replace("{hour_ago}", hourAgo);
    }

    public String buildSystemPrompt(String question) {
        List<MetricMatchResult> matches = metricSemanticParser != null
                ? metricSemanticParser.parse(question)
                : Collections.emptyList();
        List<QAExample> examples = ragRetriever != null
                ? ragRetriever.retrieve(question, DEFAULT_TOP_K)
                : Collections.emptyList();
        return buildSystemPrompt(matches, examples);
    }

    public String buildSystemPrompt(List<MetricMatchResult> matches, List<QAExample> examples) {
        String basePrompt = buildSystemPrompt();
        String metricContent = metricSemanticParser != null
                ? metricSemanticParser.formatForPrompt(matches, DEFAULT_TOP_K)
                : "（未命中任何预定义指标，请根据 schema 推断）";
        String ragExamples = ragRetriever != null
                ? ragRetriever.formatForPrompt(examples)
                : "（暂无历史相似问题）";
        return basePrompt
                .replace("{metric_content}", metricContent)
                .replace("{rag_examples}", ragExamples);
    }

    public String buildUserPrompt(String question) {
        return "用户问题：" + question + "\n\n请将这个问题转换成 Doris SQL 查询，并返回图表配置。";
    }

    void setMetricSemanticParser(MetricSemanticParser metricSemanticParser) {
        this.metricSemanticParser = metricSemanticParser;
    }

    void setRagRetriever(RagRetriever ragRetriever) {
        this.ragRetriever = ragRetriever;
    }

    void setSchemaConfig(AiSchemaConfig schemaConfig) {
        this.schemaConfig = schemaConfig;
    }

    private String loadTemplate() {
        try {
            java.nio.file.Path path = Paths.get("src/main/resources/ai/prompt_template.txt");
            if (Files.exists(path)) {
                byte[] bytes = Files.readAllBytes(path);
                return new String(bytes, StandardCharsets.UTF_8);
            }
            ClassPathResource resource = new ClassPathResource("ai/prompt_template.txt");
            try (java.io.InputStream is = resource.getInputStream()) {
                java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
                byte[] buffer = new byte[4096];
                int read;
                while ((read = is.read(buffer)) != -1) {
                    baos.write(buffer, 0, read);
                }
                return baos.toString(StandardCharsets.UTF_8.name());
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to load prompt template", e);
        }
    }
}
