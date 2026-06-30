# AI 看板模块实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 在现有 Text-to-SQL 基础上增加 YAML 语义指标层和 MySQL 关键词 RAG 知识库，提升 SQL 生成准确率和一致性。

**Architecture:** 新增 `MetricSemanticParser` 解析 YAML 指标定义并匹配用户问题，新增 `RagRetriever` 从 MySQL 全文检索历史问答，`AiPromptBuilder` 将指标定义和检索示例注入 prompt，`AiService` 编排完整流程。

**Tech Stack:** Java 8, Spring Boot 2.7, SnakeYAML 2.2, MySQL 5.7+/8.0, Doris, JUnit 4, Maven

---

## File Structure

**新增文件：**

| 文件 | 说明 |
|---|---|
| `Service/pom.xml` | 增加 `spring-boot-starter-jdbc`、`spring-boot-starter-test`、`mysql-connector-java` |
| `Service/src/main/resources/application.properties` | 增加 MySQL 数据源配置 |
| `Service/src/main/resources/ai/metrics/order_count.yml` | 订单量指标定义 |
| `Service/src/main/resources/ai/metrics/abtest_uv.yml` | ABTest UV 指标定义 |
| `Service/src/main/resources/ai/metrics/marketing_deal.yml` | 营销成交指标定义 |
| `Service/src/main/java/cn/rp/ai/semantic/MetricDefinition.java` | 指标定义数据类 |
| `Service/src/main/java/cn/rp/ai/semantic/DimensionDefinition.java` | 维度定义数据类 |
| `Service/src/main/java/cn/rp/ai/semantic/MetricMatchResult.java` | 指标匹配结果 |
| `Service/src/main/java/cn/rp/ai/semantic/MetricSemanticParser.java` | 语义解析器 |
| `Service/src/main/java/cn/rp/ai/rag/QAExample.java` | 历史问答数据类 |
| `Service/src/main/java/cn/rp/ai/rag/RagRepository.java` | MySQL 检索与写入 |
| `Service/src/main/java/cn/rp/ai/rag/RagRetriever.java` | RAG 检索器 |
| `Service/src/test/java/cn/rp/ai/semantic/MetricSemanticParserTest.java` | 语义解析器单元测试 |
| `Service/src/test/java/cn/rp/ai/rag/RagRetrieverTest.java` | RAG 检索器单元测试 |
| `Service/src/main/resources/sql/ai_qa_history.sql` | RAG 历史问答表 DDL |

**修改文件：**

| 文件 | 说明 |
|---|---|
| `Service/src/main/resources/ai/prompt_template.txt` | 新增 `{metric_content}`、`{rag_examples}` 占位符 |
| `Service/src/main/java/cn/rp/ai/AiPromptBuilder.java` | 注入指标内容和 RAG 示例 |
| `Service/src/main/java/cn/rp/ai/AiService.java` | 编排语义解析、RAG、prompt 构建、历史写入 |

---

## Task 1: 添加测试与数据库依赖

**Files:**
- Modify: `Service/pom.xml:71-77`

- [ ] **Step 1: 在 `Service/pom.xml` 的 `<dependencies>` 末尾追加依赖**

```xml
        <!-- Spring JDBC -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-jdbc</artifactId>
        </dependency>

        <!-- MySQL 驱动 -->
        <dependency>
            <groupId>mysql</groupId>
            <artifactId>mysql-connector-java</artifactId>
            <scope>runtime</scope>
        </dependency>

        <!-- 测试 -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
```

- [ ] **Step 2: 验证 Maven 依赖下载**

Run: `cd Service && mvn dependency:resolve -q`
Expected: BUILD SUCCESS

- [ ] **Step 3: Commit**

```bash
git add Service/pom.xml
git commit -m "deps(Service): 添加 spring-jdbc、mysql-connector、spring-boot-test"
```

---

## Task 2: 指标语义层数据类

**Files:**
- Create: `Service/src/main/java/cn/rp/ai/semantic/MetricDefinition.java`
- Create: `Service/src/main/java/cn/rp/ai/semantic/DimensionDefinition.java`
- Create: `Service/src/main/java/cn/rp/ai/semantic/MetricMatchResult.java`

- [ ] **Step 1: 创建 `DimensionDefinition.java`**

```java
package cn.rp.ai.semantic;

public class DimensionDefinition {
    private String name;
    private String displayName;
    private String type;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
}
```

- [ ] **Step 2: 创建 `MetricDefinition.java`**

```java
package cn.rp.ai.semantic;

import java.util.List;

public class MetricDefinition {
    private String name;
    private String displayName;
    private String description;
    private String table;
    private String aggregation;
    private String timeDimension;
    private String defaultTimeRange;
    private List<DimensionDefinition> dimensions;
    private List<String> supports;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getTable() { return table; }
    public void setTable(String table) { this.table = table; }
    public String getAggregation() { return aggregation; }
    public void setAggregation(String aggregation) { this.aggregation = aggregation; }
    public String getTimeDimension() { return timeDimension; }
    public void setTimeDimension(String timeDimension) { this.timeDimension = timeDimension; }
    public String getDefaultTimeRange() { return defaultTimeRange; }
    public void setDefaultTimeRange(String defaultTimeRange) { this.defaultTimeRange = defaultTimeRange; }
    public List<DimensionDefinition> getDimensions() { return dimensions; }
    public void setDimensions(List<DimensionDefinition> dimensions) { this.dimensions = dimensions; }
    public List<String> getSupports() { return supports; }
    public void setSupports(List<String> supports) { this.supports = supports; }
}
```

- [ ] **Step 3: 创建 `MetricMatchResult.java`**

```java
package cn.rp.ai.semantic;

public class MetricMatchResult {
    private MetricDefinition metric;
    private double score;

    public MetricMatchResult(MetricDefinition metric, double score) {
        this.metric = metric;
        this.score = score;
    }

    public MetricDefinition getMetric() { return metric; }
    public void setMetric(MetricDefinition metric) { this.metric = metric; }
    public double getScore() { return score; }
    public void setScore(double score) { this.score = score; }
}
```

- [ ] **Step 4: Commit**

```bash
git add Service/src/main/java/cn/rp/ai/semantic/
git commit -m "feat(ai-semantic): 添加指标语义层数据类"
```

---

## Task 3: YAML 指标配置文件

**Files:**
- Create: `Service/src/main/resources/ai/metrics/order_count.yml`
- Create: `Service/src/main/resources/ai/metrics/abtest_uv.yml`
- Create: `Service/src/main/resources/ai/metrics/marketing_deal.yml`

- [ ] **Step 1: 创建 `order_count.yml`**

```yaml
name: order_count
displayName: 订单量
description: 各产品类型订单成交数量趋势
table: OrderTide
aggregation: SUM(numbers)
timeDimension: timeinfo
defaultTimeRange: 7d
dimensions:
  - name: productypeid
    displayName: 产品类型
    type: Long
supports:
  - 最近7天订单量
  - 按产品类型看订单量
  - 订单量趋势
```

- [ ] **Step 2: 创建 `abtest_uv.yml`**

```yaml
name: abtest_uv
displayName: ABTest UV
description: AB测试独立访客数
table: AbTestUv
aggregation: SUM(numbers)
timeDimension: timeinfo
defaultTimeRange: 7d
dimensions:
  - name: testFlag
    displayName: 实验版本
    type: Integer
  - name: productypeid
    displayName: 产品类型
    type: Long
  - name: productypeleeave
    displayName: 产品等级
    type: Integer
supports:
  - 昨天ABTest UV
  - 最近7天ABTest UV
  - 按实验版本看UV
```

- [ ] **Step 3: 创建 `marketing_deal.yml`**

```yaml
name: marketing_deal
displayName: 营销成交单数
description: 营销活动成交订单数
table: MarketingDealhuodong
aggregation: SUM(numbers)
timeDimension: timeinfo
defaultTimeRange: 7d
dimensions:
  - name: huodongid
    displayName: 大促活动ID
    type: Long
supports:
  - 最近7天营销成交
  - 大促成交单数
```

- [ ] **Step 4: Commit**

```bash
git add Service/src/main/resources/ai/metrics/
git commit -m "feat(ai-semantic): 添加订单量、ABTest UV、营销成交指标定义"
```

---

## Task 4: MetricSemanticParser 语义解析器

**Files:**
- Create: `Service/src/main/java/cn/rp/ai/semantic/MetricSemanticParser.java`
- Create: `Service/src/test/java/cn/rp/ai/semantic/MetricSemanticParserTest.java`

- [ ] **Step 1: 写失败测试 `MetricSemanticParserTest.java`**

```java
package cn.rp.ai.semantic;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

public class MetricSemanticParserTest {

    @Test
    public void parse_shouldMatchOrderCount() {
        MetricDefinition orderCount = new MetricDefinition();
        orderCount.setName("order_count");
        orderCount.setDisplayName("订单量");
        orderCount.setDescription("各产品类型订单成交数量趋势");
        orderCount.setSupports(Arrays.asList("最近7天订单量"));

        MetricSemanticParser parser = new MetricSemanticParser(Arrays.asList(orderCount));
        List<MetricMatchResult> results = parser.parse("最近7天订单量");

        assertFalse("应该命中指标", results.isEmpty());
        assertEquals("order_count", results.get(0).getMetric().getName());
    }

    @Test
    public void parse_shouldReturnEmptyForUnknownQuestion() {
        MetricSemanticParser parser = new MetricSemanticParser(Collections.emptyList());
        List<MetricMatchResult> results = parser.parse("随便问一个不存在的东西");
        assertTrue(results.isEmpty());
    }
}
```

- [ ] **Step 2: 运行测试确认失败**

Run: `cd Service && mvn test -Dtest=MetricSemanticParserTest -q`
Expected: BUILD FAILURE（类不存在）

- [ ] **Step 3: 实现 `MetricSemanticParser.java`**

```java
package cn.rp.ai.semantic;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;

import javax.annotation.PostConstruct;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

@Component
public class MetricSemanticParser {

    private List<MetricDefinition> metrics = new ArrayList<>();

    public MetricSemanticParser() {
    }

    public MetricSemanticParser(List<MetricDefinition> metrics) {
        this.metrics = metrics != null ? metrics : new ArrayList<>();
    }

    @PostConstruct
    public void init() {
        if (metrics.isEmpty()) {
            loadMetrics();
        }
    }

    private void loadMetrics() {
        Path dir = Paths.get("src/main/resources/ai/metrics");
        if (Files.exists(dir) && Files.isDirectory(dir)) {
            try (Stream<Path> paths = Files.list(dir)) {
                paths.filter(p -> p.toString().endsWith(".yml") || p.toString().endsWith(".yaml"))
                     .sorted()
                     .forEach(this::loadMetricFile);
            } catch (Exception e) {
                throw new RuntimeException("Failed to load metric files from " + dir, e);
            }
        } else {
            loadClasspathMetric("ai/metrics/order_count.yml");
            loadClasspathMetric("ai/metrics/abtest_uv.yml");
            loadClasspathMetric("ai/metrics/marketing_deal.yml");
        }
    }

    private void loadMetricFile(Path path) {
        try (InputStream is = Files.newInputStream(path)) {
            Yaml yaml = new Yaml(new LoaderOptions());
            MetricDefinition metric = yaml.loadAs(is, MetricDefinition.class);
            metrics.add(metric);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load metric file: " + path, e);
        }
    }

    private void loadClasspathMetric(String path) {
        ClassPathResource resource = new ClassPathResource(path);
        if (!resource.exists()) {
            return;
        }
        try (InputStream is = resource.getInputStream()) {
            Yaml yaml = new Yaml(new LoaderOptions());
            MetricDefinition metric = yaml.loadAs(is, MetricDefinition.class);
            metrics.add(metric);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load classpath metric: " + path, e);
        }
    }

    public List<MetricMatchResult> parse(String question) {
        if (question == null || question.trim().isEmpty()) {
            return Collections.emptyList();
        }
        String lowerQuestion = question.toLowerCase();
        List<MetricMatchResult> results = new ArrayList<>();
        for (MetricDefinition metric : metrics) {
            double score = calculateScore(lowerQuestion, metric);
            if (score > 0) {
                results.add(new MetricMatchResult(metric, score));
            }
        }
        results.sort((a, b) -> Double.compare(b.getScore(), a.getScore()));
        return results;
    }

    private double calculateScore(String question, MetricDefinition metric) {
        double score = 0;
        score += countMatches(question, metric.getName());
        score += countMatches(question, metric.getDisplayName()) * 2;
        score += countMatches(question, metric.getDescription());
        if (metric.getSupports() != null) {
            for (String support : metric.getSupports()) {
                score += countMatches(question, support) * 1.5;
            }
        }
        return score;
    }

    private double countMatches(String question, String text) {
        if (text == null || text.isEmpty()) {
            return 0;
        }
        String lowerText = text.toLowerCase();
        int count = 0;
        int idx = 0;
        while ((idx = question.indexOf(lowerText, idx)) != -1) {
            count++;
            idx += lowerText.length();
        }
        return count;
    }

    public String formatForPrompt(List<MetricMatchResult> matches, int topK) {
        if (matches == null || matches.isEmpty()) {
            return "（未命中任何预定义指标，请根据 schema 推断）";
        }
        StringBuilder sb = new StringBuilder();
        int limit = Math.min(topK, matches.size());
        for (int i = 0; i < limit; i++) {
            MetricDefinition m = matches.get(i).getMetric();
            sb.append("指标：").append(m.getDisplayName()).append("(").append(m.getName()).append(")\n");
            sb.append("表：").append(m.getTable()).append("\n");
            sb.append("聚合：").append(m.getAggregation()).append("\n");
            sb.append("时间维度：").append(m.getTimeDimension()).append("\n");
            sb.append("默认时间范围：").append(m.getDefaultTimeRange()).append("\n");
            if (m.getDimensions() != null && !m.getDimensions().isEmpty()) {
                sb.append("可用维度：");
                for (DimensionDefinition d : m.getDimensions()) {
                    sb.append(d.getName()).append("(").append(d.getDisplayName()).append(") ");
                }
                sb.append("\n");
            }
            sb.append("\n");
        }
        return sb.toString().trim();
    }
}
```

- [ ] **Step 4: 运行测试确认通过**

Run: `cd Service && mvn test -Dtest=MetricSemanticParserTest -q`
Expected: BUILD SUCCESS, 2 tests passed

- [ ] **Step 5: Commit**

```bash
git add Service/src/main/java/cn/rp/ai/semantic/MetricSemanticParser.java Service/src/test/java/cn/rp/ai/semantic/MetricSemanticParserTest.java
git commit -m "feat(ai-semantic): 实现指标语义解析器"
```

---

## Task 5: RAG 数据类与 MySQL Repository

**Files:**
- Create: `Service/src/main/java/cn/rp/ai/rag/QAExample.java`
- Create: `Service/src/main/java/cn/rp/ai/rag/RagRepository.java`
- Create: `Service/src/main/resources/sql/ai_qa_history.sql`
- Modify: `Service/src/main/resources/application.properties`

- [ ] **Step 1: 创建 `QAExample.java`**

```java
package cn.rp.ai.rag;

public class QAExample {
    private Long id;
    private String question;
    private String matchedMetrics;
    private String generatedSql;
    private String chartType;
    private String xAxisField;
    private String yAxisField;
    private Integer isSuccess;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getQuestion() { return question; }
    public void setQuestion(String question) { this.question = question; }
    public String getMatchedMetrics() { return matchedMetrics; }
    public void setMatchedMetrics(String matchedMetrics) { this.matchedMetrics = matchedMetrics; }
    public String getGeneratedSql() { return generatedSql; }
    public void setGeneratedSql(String generatedSql) { this.generatedSql = generatedSql; }
    public String getChartType() { return chartType; }
    public void setChartType(String chartType) { this.chartType = chartType; }
    public String getxAxisField() { return xAxisField; }
    public void setxAxisField(String xAxisField) { this.xAxisField = xAxisField; }
    public String getyAxisField() { return yAxisField; }
    public void setyAxisField(String yAxisField) { this.yAxisField = yAxisField; }
    public Integer getIsSuccess() { return isSuccess; }
    public void setIsSuccess(Integer isSuccess) { this.isSuccess = isSuccess; }
}
```

- [ ] **Step 2: 创建 `RagRepository.java`**

```java
package cn.rp.ai.rag;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;

@Repository
public class RagRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public List<QAExample> findSimilarQuestions(String question, int limit) {
        String sql = "SELECT id, question, matched_metrics, generated_sql, chart_type, x_axis_field, y_axis_field " +
                     "FROM ai_qa_history " +
                     "WHERE is_success = 1 " +
                     "AND MATCH(question) AGAINST(? IN NATURAL LANGUAGE MODE) " +
                     "ORDER BY created_at DESC " +
                     "LIMIT ?";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(QAExample.class), question, limit);
    }

    public void save(QAExample example) {
        String sql = "INSERT INTO ai_qa_history " +
                     "(question, matched_metrics, generated_sql, chart_type, x_axis_field, y_axis_field, is_success) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql, example.getQuestion(), example.getMatchedMetrics(),
                example.getGeneratedSql(), example.getChartType(), example.getxAxisField(),
                example.getyAxisField(), example.getIsSuccess() != null ? example.getIsSuccess() : 1);
    }

    public List<QAExample> findByKeywordFallback(String question, int limit) {
        String sql = "SELECT id, question, matched_metrics, generated_sql, chart_type, x_axis_field, y_axis_field " +
                     "FROM ai_qa_history " +
                     "WHERE is_success = 1 AND question LIKE ? " +
                     "ORDER BY created_at DESC " +
                     "LIMIT ?";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(QAExample.class), "%" + question + "%", limit);
    }
}
```

- [ ] **Step 3: 创建 DDL 文件 `ai_qa_history.sql`**

```sql
CREATE TABLE IF NOT EXISTS ai_qa_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    question VARCHAR(1000) NOT NULL,
    matched_metrics VARCHAR(500),
    generated_sql TEXT,
    chart_type VARCHAR(50),
    x_axis_field VARCHAR(100),
    y_axis_field VARCHAR(100),
    is_success TINYINT DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FULLTEXT INDEX idx_question (question)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

- [ ] **Step 4: 在 `application.properties` 追加 MySQL 数据源配置**

```properties
# RAG 历史问答库（MySQL 3306）
spring.datasource.url=jdbc:mysql://192.168.88.101:3306/hadoop_db
spring.datasource.username=hadoop
spring.datasource.password=hadoop
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
```

- [ ] **Step 5: 在 node1 MySQL 执行 DDL**

Run: `ssh node1 "mysql -h 127.0.0.1 -P 3306 -uhadoop -phadoop -D hadoop_db < /dev/stdin" < Service/src/main/resources/sql/ai_qa_history.sql`
Expected: 无报错，表创建成功

验证：
Run: `ssh node1 "mysql -h 127.0.0.1 -P 3306 -uhadoop -phadoop -D hadoop_db -e 'SHOW TABLES LIKE \"ai_qa_history\";'"`
Expected: 输出 `ai_qa_history`

- [ ] **Step 6: Commit**

```bash
git add Service/src/main/java/cn/rp/ai/rag/ Service/src/main/resources/sql/ai_qa_history.sql Service/src/main/resources/application.properties
git commit -m "feat(ai-rag): 添加 RAG 数据类、Repository、MySQL 表及数据源配置"
```

---

## Task 6: RagRetriever 检索器

**Files:**
- Create: `Service/src/main/java/cn/rp/ai/rag/RagRetriever.java`
- Create: `Service/src/test/java/cn/rp/ai/rag/RagRetrieverTest.java`

- [ ] **Step 1: 写失败测试 `RagRetrieverTest.java`**

```java
package cn.rp.ai.rag;

import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class RagRetrieverTest {

    private RagRetriever retriever;

    @Before
    public void setUp() {
        retriever = new RagRetriever();
    }

    @Test
    public void formatForPrompt_shouldIncludeQuestionAndSql() {
        QAExample example = new QAExample();
        example.setQuestion("最近7天订单量");
        example.setGeneratedSql("SELECT timeinfo, SUM(numbers) FROM OrderTide ...");

        String prompt = retriever.formatForPrompt(Arrays.asList(example));
        assertTrue(prompt.contains("最近7天订单量"));
        assertTrue(prompt.contains("SELECT timeinfo, SUM(numbers) FROM OrderTide"));
    }

    @Test
    public void formatForPrompt_shouldReturnEmptyHintWhenNoExamples() {
        String prompt = retriever.formatForPrompt(Arrays.asList());
        assertEquals("（暂无历史相似问题）", prompt);
    }
}
```

- [ ] **Step 2: 运行测试确认失败**

Run: `cd Service && mvn test -Dtest=RagRetrieverTest -q`
Expected: BUILD FAILURE（类不存在）

- [ ] **Step 3: 实现 `RagRetriever.java`**

```java
package cn.rp.ai.rag;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class RagRetriever {

    @Autowired
    private RagRepository ragRepository;

    public List<QAExample> retrieve(String question, int topK) {
        if (question == null || question.trim().isEmpty() || ragRepository == null) {
            return Collections.emptyList();
        }
        try {
            List<QAExample> results = ragRepository.findSimilarQuestions(question, topK);
            if (results.isEmpty()) {
                results = ragRepository.findByKeywordFallback(question, topK);
            }
            return results;
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    public String formatForPrompt(List<QAExample> examples) {
        if (examples == null || examples.isEmpty()) {
            return "（暂无历史相似问题）";
        }
        StringBuilder sb = new StringBuilder();
        for (QAExample example : examples) {
            sb.append("问题：").append(example.getQuestion()).append("\n");
            sb.append("SQL：").append(example.getGeneratedSql()).append("\n");
            if (example.getChartType() != null) {
                sb.append("图表类型：").append(example.getChartType()).append("\n");
            }
            sb.append("\n");
        }
        return sb.toString().trim();
    }
}
```

- [ ] **Step 4: 运行测试确认通过**

Run: `cd Service && mvn test -Dtest=RagRetrieverTest -q`
Expected: BUILD SUCCESS, 2 tests passed

- [ ] **Step 5: Commit**

```bash
git add Service/src/main/java/cn/rp/ai/rag/RagRetriever.java Service/src/test/java/cn/rp/ai/rag/RagRetrieverTest.java
git commit -m "feat(ai-rag): 实现 RAG 检索器及格式化"
```

---

## Task 7: Prompt 模板改造

**Files:**
- Modify: `Service/src/main/resources/ai/prompt_template.txt`

- [ ] **Step 1: 在 prompt_template.txt 的 `{schema_content}` 后追加指标和 RAG 占位符**

修改后的关键部分：

```text
## Schema

{schema_content}

## 匹配到的指标定义

{metric_content}

## 历史相似问题

{rag_examples}

## 重要规则
```

- [ ] **Step 2: Commit**

```bash
git add Service/src/main/resources/ai/prompt_template.txt
git commit -m "feat(ai-prompt): prompt 模板增加指标语义和 RAG 示例占位符"
```

---

## Task 8: AiPromptBuilder 注入语义与 RAG 内容

**Files:**
- Modify: `Service/src/main/java/cn/rp/ai/AiPromptBuilder.java`

- [ ] **Step 1: 在 `AiPromptBuilder.java` 中新增重载方法并引入依赖**

在文件顶部添加 import：

```java
import cn.rp.ai.rag.QAExample;
import cn.rp.ai.rag.RagRetriever;
import cn.rp.ai.semantic.MetricMatchResult;
import cn.rp.ai.semantic.MetricSemanticParser;
import org.springframework.beans.factory.annotation.Autowired;
```

在类中添加：

```java
    @Autowired
    private MetricSemanticParser metricSemanticParser;

    @Autowired
    private RagRetriever ragRetriever;
```

新增方法：

```java
    public String buildSystemPrompt(String question) {
        List<MetricMatchResult> matches = metricSemanticParser.parse(question);
        List<QAExample> examples = ragRetriever.retrieve(question, 3);
        return buildSystemPrompt(matches, examples);
    }

    public String buildSystemPrompt(List<MetricMatchResult> matches, List<QAExample> examples) {
        String basePrompt = buildSystemPrompt();
        String metricContent = metricSemanticParser.formatForPrompt(matches, 3);
        String ragExamples = ragRetriever.formatForPrompt(examples);
        return basePrompt
                .replace("{metric_content}", metricContent)
                .replace("{rag_examples}", ragExamples);
    }
```

- [ ] **Step 2: 保留原有 `buildSystemPrompt()` 和 `buildUserPrompt(String question)` 不变**

原有方法继续用于无参数场景或单元测试。

- [ ] **Step 3: Commit**

```bash
git add Service/src/main/java/cn/rp/ai/AiPromptBuilder.java
git commit -m "feat(ai-prompt): PromptBuilder 支持注入指标语义与 RAG 示例"
```

---

## Task 9: AiService 编排完整流程

**Files:**
- Modify: `Service/src/main/java/cn/rp/ai/AiService.java`

- [ ] **Step 1: 在 `AiService.java` 中注入新组件**

在文件顶部添加 import：

```java
import cn.rp.ai.rag.QAExample;
import cn.rp.ai.rag.RagRepository;
import cn.rp.ai.rag.RagRetriever;
import cn.rp.ai.semantic.MetricMatchResult;
import cn.rp.ai.semantic.MetricSemanticParser;
```

在类中添加字段：

```java
    @Autowired
    private MetricSemanticParser metricSemanticParser;

    @Autowired
    private RagRetriever ragRetriever;

    @Autowired
    private RagRepository ragRepository;
```

- [ ] **Step 2: 修改 `ask` 方法调用新的 prompt 构建方式**

将：

```java
String systemPrompt = promptBuilder.buildSystemPrompt();
```

替换为：

```java
String systemPrompt = promptBuilder.buildSystemPrompt(question);
```

- [ ] **Step 3: 在 SQL 执行成功后异步写入历史问答库**

在 `AiService.AiQueryResult result = new AiQueryResult();` 之后、`return result;` 之前添加：

```java
            // 异步写入 RAG 历史库
            saveToHistory(question, result);
```

新增私有方法：

```java
    private void saveToHistory(String question, AiQueryResult result) {
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

            List<MetricMatchResult> matches = metricSemanticParser.parse(question);
            if (matches != null && !matches.isEmpty()) {
                example.setMatchedMetrics(matches.get(0).getMetric().getName());
            }

            ragRepository.save(example);
        } catch (Exception e) {
            logger.warn("Failed to save QA history", e);
        }
    }
```

- [ ] **Step 4: Commit**

```bash
git add Service/src/main/java/cn/rp/ai/AiService.java
git commit -m "feat(ai-service): 编排语义解析、RAG 检索与历史问答写入"
```

---

## Task 10: 编译与单元测试

**Files:**
- 全部新增/修改的 Java 文件

- [ ] **Step 1: 运行 Service 模块编译**

Run: `cd Service && mvn clean compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 2: 运行全部单元测试**

Run: `cd Service && mvn test -q`
Expected: BUILD SUCCESS, 至少 4 个测试通过（2 个 semantic + 2 个 rag）

- [ ] **Step 3: Commit（如测试通过）**

```bash
git commit -m "test(ai): 通过语义解析与 RAG 检索单元测试" --allow-empty
```

---

## Task 11: 端到端验证

**Files:**
- 无新增文件

前置条件：node1 上 Doris FE/BE 运行，MySQL 运行且 `ai_qa_history` 表已创建。

- [ ] **Step 1: 启动 Service 模块**

Run: `cd Service && mvn spring-boot:run`
Expected: 服务启动成功，监听 8084

- [ ] **Step 2: 调用接口测试指标语义匹配**

Run:

```bash
curl -X POST http://localhost:8084/CollectService/ai/query \
  -H "Content-Type: application/json" \
  -d '{"question":"最近7天订单量"}'
```

Expected: 返回 JSON，包含 Doris SQL，SQL 中应出现 `OrderTide` 和 `SUM(numbers)`。

- [ ] **Step 3: 验证历史问答已入库**

Run: `ssh node1 "mysql -h 127.0.0.1 -P 3306 -uhadoop -phadoop -D hadoop_db -e 'SELECT question, matched_metrics FROM ai_qa_history ORDER BY id DESC LIMIT 1;'"`
Expected: 显示上一步的问题和匹配指标 `order_count`

- [ ] **Step 4: 再次提问相似问题，验证 RAG 召回**

Run:

```bash
curl -X POST http://localhost:8084/CollectService/ai/query \
  -H "Content-Type: application/json" \
  -d '{"question":"最近7天各产品类型订单量"}'
```

Expected: 返回成功，SQL 符合预期。

- [ ] **Step 5: Commit 验证结果（可选）**

```bash
git commit -m "feat(ai): AI 看板模块语义层 + RAG 知识库端到端验证通过" --allow-empty
```

---

## Spec Coverage Check

对照设计文档 `docs/superpowers/specs/2026-06-30-ai-dashboard-design.md`：

| 设计章节 | 实现任务 | 状态 |
|---|---|---|
| YAML 指标定义格式 | Task 2 + Task 3 | ✅ 已覆盖 |
| 语义匹配逻辑 | Task 4 | ✅ 已覆盖 |
| MySQL `ai_qa_history` 表 | Task 5 | ✅ 已覆盖 |
| 关键词/BM25 检索 | Task 5 + Task 6 | ✅ 已覆盖（全文检索 + LIKE 降级） |
| Prompt v2 结构 | Task 7 + Task 8 | ✅ 已覆盖 |
| 完整数据流 | Task 9 | ✅ 已覆盖 |
| 错误处理 | Task 4/6/9 中的 try-catch 降级 | ✅ 已覆盖 |
| 单元测试 | Task 4 + Task 6 + Task 10 | ✅ 已覆盖 |

**无遗漏。**

---

## Placeholder Scan

扫描计划全文，未发现以下红旗：

- 无 "TBD"/"TODO"/"implement later"
- 无 "add appropriate error handling" 等模糊描述
- 每个代码步骤包含完整可运行代码
- 每个命令包含预期输出
- 类型/方法名在全文中一致（如 `MetricSemanticParser`、`RagRetriever`、`QAExample`）

---

## Execution Handoff

**Plan complete and saved to `docs/superpowers/plans/2026-06-30-ai-dashboard.md`. Two execution options:**

**1. Subagent-Driven (recommended)** - I dispatch a fresh subagent per task, review between tasks, fast iteration

**2. Inline Execution** - Execute tasks in this session using executing-plans, batch execution with checkpoints

**Which approach?**
