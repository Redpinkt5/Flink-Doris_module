package cn.rp.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.Yaml;

import javax.annotation.PostConstruct;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

/**
 * AI 语义层配置加载器
 * 从 schema.yaml 加载表结构和字段定义
 */
@Component
public class AiSchemaConfig {

    private static final Logger logger = LoggerFactory.getLogger(AiSchemaConfig.class);

    private Map<String, Object> schemaConfig;

    @PostConstruct
    public void init() {
        loadSchema();
    }

    private void loadSchema() {
        try {
            Yaml yaml = new Yaml();
            // 优先从文件路径加载（支持热更新）
            Path schemaPath = Paths.get("src/main/resources/ai/schema.yaml");
            if (Files.exists(schemaPath)) {
                try (InputStream is = Files.newInputStream(schemaPath)) {
                    schemaConfig = yaml.load(is);
                    logger.info("AI Schema loaded from file: {}", schemaPath.toAbsolutePath());
                }
            } else {
                // 回退到 classpath
                try (InputStream is = getClass().getClassLoader().getResourceAsStream("ai/schema.yaml")) {
                    if (is != null) {
                        schemaConfig = yaml.load(is);
                        logger.info("AI Schema loaded from classpath");
                    } else {
                        logger.error("schema.yaml not found in classpath or file system");
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Failed to load AI schema", e);
        }
    }

    @SuppressWarnings("unchecked")
    public String getTableSchemaForPrompt() {
        if (schemaConfig == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        var tables = (java.util.List<Map<String, Object>>) schemaConfig.get("tables");
        if (tables == null) {
            return "";
        }
        for (Map<String, Object> table : tables) {
            sb.append("表名：").append(table.get("tableName"))
              .append("，业务域：").append(table.get("businessDomain"))
              .append("，说明：").append(table.get("description")).append("\n");
            var fields = (java.util.List<Map<String, Object>>) table.get("fields");
            if (fields != null) {
                for (Map<String, Object> field : fields) {
                    String fieldName = String.valueOf(field.get("fieldName"));
                    String type = String.valueOf(field.get("type"));
                    String desc = String.valueOf(field.get("description"));
                    StringBuilder line = new StringBuilder();
                    line.append("  - ").append(fieldName)
                        .append(" (").append(type).append("): ").append(desc);
                    if (field.get("isMetric") != null && Boolean.TRUE.equals(field.get("isMetric"))) {
                        line.append(" [指标]");
                    }
                    if (field.get("isDimension") != null && Boolean.TRUE.equals(field.get("isDimension"))) {
                        line.append(" [维度]");
                        var mapping = (Map<String, Object>) field.get("valueMapping");
                        if (mapping != null && !mapping.isEmpty()) {
                            line.append("，值域：").append(mapping);
                        }
                    }
                    sb.append(line).append("\n");
                }
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> getQueryRules() {
        if (schemaConfig == null) {
            return Map.of();
        }
        Map<String, Object> rules = (Map<String, Object>) schemaConfig.get("queryRules");
        return rules != null ? rules : Map.of();
    }

    public String getJdbcUrl() {
        Map<String, Object> rules = getQueryRules();
        return String.valueOf(rules.getOrDefault("jdbcUrl", "jdbc:mysql://192.168.88.101:9030/hadoop_db"));
    }

    public String getJdbcDriver() {
        Map<String, Object> rules = getQueryRules();
        return String.valueOf(rules.getOrDefault("jdbcDriver", "com.mysql.cj.jdbc.Driver"));
    }

    public int getDefaultLimit() {
        Map<String, Object> rules = getQueryRules();
        Object limit = rules.get("defaultLimit");
        return limit != null ? Integer.parseInt(String.valueOf(limit)) : 10;
    }
}
