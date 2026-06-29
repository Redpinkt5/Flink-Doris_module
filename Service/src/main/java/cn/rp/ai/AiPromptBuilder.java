package cn.rp.ai;

import cn.rp.config.AiSchemaConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * AI Prompt 构建器
 * 加载 prompt_template.txt 并注入 schema 内容及动态日期
 */
@Component
public class AiPromptBuilder {

    private static final DateTimeFormatter DAY_FMT = DateTimeFormatter.ofPattern("yyyyMMdd");

    @Autowired
    private AiSchemaConfig schemaConfig;

    private String systemPromptTemplate;

    public String buildSystemPrompt() {
        if (systemPromptTemplate == null) {
            systemPromptTemplate = loadTemplate();
        }
        String schemaContent = schemaConfig.getTableSchemaForPrompt();
        return systemPromptTemplate
                .replace("{schema_content}", schemaContent)
                .replace("{current_date}", LocalDate.now().format(DAY_FMT))
                .replace("{date_7days_ago}", LocalDate.now().minusDays(7).format(DAY_FMT))
                .replace("{date_30days_ago}", LocalDate.now().minusDays(30).format(DAY_FMT))
                .replace("{month_start}", LocalDate.now().withDayOfMonth(1).format(DAY_FMT));
    }

    public String buildUserPrompt(String question) {
        return "用户问题：" + question + "\n\n请将这个问题转换成 Doris SQL 查询，并返回图表配置。";
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
