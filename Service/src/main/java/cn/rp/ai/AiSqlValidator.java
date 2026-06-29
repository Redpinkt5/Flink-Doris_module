package cn.rp.ai;

import cn.rp.config.AiSchemaConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * SQL 安全校验器
 * 防止 SQL 注入、危险操作、全表扫描
 */
@Component
public class AiSqlValidator {

    @Autowired
    private AiSchemaConfig schemaConfig;

    // 允许的 SQL 关键字
    private static final Set<String> ALLOWED_KEYWORDS = new HashSet<>();
    // 禁止的 SQL 关键字
    private static final Set<String> BLOCKED_KEYWORDS = new HashSet<>();
    // 允许的表名（从 schema 中自动获取）
    private final Set<String> allowedTables = new HashSet<>();
    // 已知的危险 SQL 模式
    private static final Pattern DANGEROUS_PATTERN = Pattern.compile(
            ".*(DROP|DELETE|TRUNCATE|ALTER|CREATE|INSERT|UPDATE|GRANT|REVOKE|EXEC|EXECUTE|SHUTDOWN).*",
            Pattern.CASE_INSENSITIVE
    );

    static {
        ALLOWED_KEYWORDS.add("SELECT");
        ALLOWED_KEYWORDS.add("FROM");
        ALLOWED_KEYWORDS.add("WHERE");
        ALLOWED_KEYWORDS.add("AND");
        ALLOWED_KEYWORDS.add("OR");
        ALLOWED_KEYWORDS.add("ORDER");
        ALLOWED_KEYWORDS.add("BY");
        ALLOWED_KEYWORDS.add("DESC");
        ALLOWED_KEYWORDS.add("ASC");
        ALLOWED_KEYWORDS.add("LIMIT");
        ALLOWED_KEYWORDS.add("GROUP");
        ALLOWED_KEYWORDS.add("HAVING");
        ALLOWED_KEYWORDS.add("AS");
        ALLOWED_KEYWORDS.add("INNER");
        ALLOWED_KEYWORDS.add("JOIN");
        ALLOWED_KEYWORDS.add("LEFT");
        ALLOWED_KEYWORDS.add("RIGHT");
        ALLOWED_KEYWORDS.add("ON");
        ALLOWED_KEYWORDS.add("COUNT");
        ALLOWED_KEYWORDS.add("SUM");
        ALLOWED_KEYWORDS.add("AVG");
        ALLOWED_KEYWORDS.add("MAX");
        ALLOWED_KEYWORDS.add("MIN");
        ALLOWED_KEYWORDS.add("DISTINCT");
        ALLOWED_KEYWORDS.add("ROW_NUMBER");
        ALLOWED_KEYWORDS.add("OVER");
        ALLOWED_KEYWORDS.add("PARTITION");
        ALLOWED_KEYWORDS.add("LENGTH");
        ALLOWED_KEYWORDS.add("SUBSTRING");

        BLOCKED_KEYWORDS.add("DROP");
        BLOCKED_KEYWORDS.add("DELETE");
        BLOCKED_KEYWORDS.add("TRUNCATE");
        BLOCKED_KEYWORDS.add("ALTER");
        BLOCKED_KEYWORDS.add("CREATE");
        BLOCKED_KEYWORDS.add("INSERT");
        BLOCKED_KEYWORDS.add("UPDATE");
        BLOCKED_KEYWORDS.add("GRANT");
        BLOCKED_KEYWORDS.add("REVOKE");
        BLOCKED_KEYWORDS.add("EXEC");
        BLOCKED_KEYWORDS.add("EXECUTE");
        BLOCKED_KEYWORDS.add("SHUTDOWN");
        BLOCKED_KEYWORDS.add("INTO OUTFILE");
        BLOCKED_KEYWORDS.add("INTO DUMPFILE");
    }

    /**
     * 校验 SQL 是否安全且有效
     * @return 校验结果，包含错误信息（如果有）
     */
    public ValidationResult validate(String sql) {
        if (sql == null || sql.trim().isEmpty()) {
            return ValidationResult.fail("SQL 不能为空");
        }

        String trimmedSql = sql.trim();

        // 1. 必须以 SELECT 开头
        if (!trimmedSql.toUpperCase().startsWith("SELECT")) {
            return ValidationResult.fail("只允许 SELECT 查询操作");
        }

        // 2. 检查危险关键字
        if (DANGEROUS_PATTERN.matcher(trimmedSql).matches()) {
            return ValidationResult.fail("SQL 包含禁止的关键字或操作");
        }
        for (String blocked : BLOCKED_KEYWORDS) {
            if (Pattern.compile("\\b" + blocked + "\\b", Pattern.CASE_INSENSITIVE).matcher(trimmedSql).find()) {
                return ValidationResult.fail("SQL 包含禁止的关键字: " + blocked);
            }
        }

        // 3. 检查是否只有 SELECT（防止其他操作通过大小写绕过）
        String upperSql = trimmedSql.toUpperCase();
        if (!upperSql.startsWith("SELECT")) {
            return ValidationResult.fail("仅允许 SELECT 查询");
        }

        // 4. 检查是否有合理的 LIMIT
        if (!Pattern.compile("LIMIT\\s+\\d+", Pattern.CASE_INSENSITIVE).matcher(trimmedSql).find()) {
            return ValidationResult.fail("必须包含 LIMIT 限制返回条数");
        }

        // 5. 检查 LIMIT 值是否合理（不超过 1000）
        var limitMatcher = Pattern.compile("LIMIT\\s+(\\d+)", Pattern.CASE_INSENSITIVE).matcher(trimmedSql);
        if (limitMatcher.find()) {
            int limitValue = Integer.parseInt(limitMatcher.group(1));
            if (limitValue > 1000) {
                return ValidationResult.fail("LIMIT 值不能超过 1000");
            }
        }

        // 6. 检查是否查询了允许的表
        if (!containsAllowedTable(trimmedSql)) {
            return ValidationResult.fail("SQL 必须查询已配置的表");
        }

        return ValidationResult.ok();
    }

    private boolean containsAllowedTable(String sql) {
        if (allowedTables.isEmpty()) {
            return true; // 如果没有配置表白名单，放行（安全由其他规则兜底）
        }
        String upperSql = sql.toUpperCase();
        for (String table : allowedTables) {
            if (Pattern.compile("\\b" + table.toUpperCase() + "\\b").matcher(upperSql).find()) {
                return true;
            }
        }
        return false;
    }

    public void addAllowedTable(String tableName) {
        allowedTables.add(tableName);
    }

    public void addAllowedTables(String... tableNames) {
        for (String name : tableNames) {
            allowedTables.add(name);
        }
    }

    public static class ValidationResult {
        private final boolean valid;
        private final String errorMessage;

        private ValidationResult(boolean valid, String errorMessage) {
            this.valid = valid;
            this.errorMessage = errorMessage;
        }

        public static ValidationResult ok() {
            return new ValidationResult(true, null);
        }

        public static ValidationResult fail(String message) {
            return new ValidationResult(false, message);
        }

        public boolean isValid() {
            return valid;
        }

        public String getErrorMessage() {
            return errorMessage;
        }
    }
}
