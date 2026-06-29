package cn.rp.ai;

import cn.rp.config.AiSchemaConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * AI Prompt 构建器
 * 加载 prompt_template.txt 并注入 schema 内容
 */
@Component
public class AiPromptBuilder {

    @Autowired
    private AiSchemaConfig schemaConfig;

    private String systemPromptTemplate;

    public String buildSystemPrompt() {
        if (systemPromptTemplate == null) {
            systemPromptTemplate = loadTemplate();
        }
        String schemaContent = schemaConfig.getTableSchemaForPrompt();
        return systemPromptTemplate.replace("{schema_content}", schemaContent);
    }

    public String buildUserPrompt(String question) {
        return "用户问题：" + question + "\n\n请将这个问题转换成 Doris SQL 查询。";
    }

    private String loadTemplate() {
        try {
            // 优先从文件路径加载
            var path = Paths.get("src/main/resources/ai/prompt_template.txt");
            if (Files.exists(path)) {
                return new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
            }
            // 回退到 classpath
            ClassPathResource resource = new ClassPathResource("ai/prompt_template.txt");
            try (var is = resource.getInputStream()) {
                return new String(is.readAllBytes(), StandardCharsets.UTF_8);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to load prompt template", e);
        }
    }
}
