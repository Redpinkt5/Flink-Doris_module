# AI 看板模块设计文档

**日期**: 2026-06-30  
**方案**: 方案 1 — YAML 语义指标层 + MySQL 关键词 RAG（轻量 MVP）  
**范围**: 后端语义层与 RAG 知识库，前端看板不在本次范围内  

---

## 1. 背景与目标

现有 `AiService` 已支持自然语言 → SQL → Doris 的 Text-to-SQL 流程，但存在两个问题：

1. **口径不一致**：LLM 直接写 SQL，容易用错表/字段/聚合方式。
2. **没有记忆**：每次提问都是冷启动，无法复用历史正确问答。

本模块目标：

- 建立**指标语义层**，把“订单量”“UV”等业务术语映射到标准口径。
- 建立**RAG 知识库**，检索历史相似问题与 SQL，增强 prompt。
- 保持轻量，不引入向量库，复用现有 Java/Spring Boot + Doris + MySQL 栈。

---

## 2. 整体架构

```
用户问题
  ↓
MetricSemanticParser（YAML 指标匹配）
  ↓
RagRetriever（MySQL 历史问答检索）
  ↓
AiPromptBuilder v2（拼装 prompt）
  ↓
AiService / DeepSeek LLM → SQL + 图表配置
  ↓
Doris 执行 → 返回结果
  ↓
成功时异步写入 ai_qa_history（反馈闭环）
```

**新增组件**:

| 组件 | 路径/位置 | 职责 |
|---|---|---|
| 指标 YAML 配置 | `Service/src/main/resources/ai/metrics/*.yml` | 定义指标口径、维度、聚合 |
| 语义解析器 | `cn.rp.ai.semantic.MetricSemanticParser` | 把问题匹配到指标 |
| RAG 检索器 | `cn.rp.ai.rag.RagRetriever` | 从 MySQL 召回相似历史问答 |
| Prompt 构建器 v2 | `cn.rp.ai.AiPromptBuilder` | 注入指标定义 + RAG 示例 |
| 历史问答表 | MySQL `ai_qa_history` | 存储成功问答对 |

**复用组件**: `AiControl`、`AiService`、`AiSqlValidator`、`DorisDBUtil`。

---

## 3. 指标语义层

### 3.1 YAML 指标定义格式

以订单量为例：

```yaml
name: order_count
displayName: 订单量
description: 各产品类型订单量趋势
table: OrderTide
aggregation: SUM(numbers)
timeDimension: timeinfo
defaultTimeRange: 7d
dimensions:
  - name: productypeid
    displayName: 产品类型
supports:
  - 最近7天订单量
  - 按产品类型看订单量
```

字段说明：

- `name`: 指标唯一标识，英文。
- `displayName`: 中文名，用于匹配用户口语。
- `description`: 业务含义，辅助 LLM 理解。
- `table`: 数据来源表。
- `aggregation`: 聚合表达式。
- `timeDimension`: 时间字段。
- `defaultTimeRange`: 默认时间范围。
- `dimensions`: 支持的维度列表。
- `supports`: 常见问法示例，用于提升匹配率。

### 3.2 语义匹配逻辑

1. 启动时加载所有 YAML 指标到内存（`Map<String, MetricDefinition>`）。
2. 用户问题进来后，对 `name`、`displayName`、`description`、`supports` 做关键词匹配。
3. 命中指标后，把“表 + 聚合 + 时间维度 + 可用维度”注入 prompt。
4. 未命中时降级为原有直接 Text-to-SQL。

---

## 4. RAG 知识库

### 4.1 MySQL 表结构

```sql
CREATE TABLE ai_qa_history (
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

### 4.2 检索逻辑

- 使用 MySQL 全文检索：`MATCH(question) AGAINST('...' IN NATURAL LANGUAGE MODE)`。
- 召回 top-3 成功历史问答（`is_success = 1`）。
- 将问题和 SQL 一起注入 prompt。

### 4.3 反馈闭环

- 查询成功且 SQL 校验通过后，异步写入 `ai_qa_history`。
- 写入失败不阻塞返回，仅记录日志。

---

## 5. Prompt 设计

Prompt v2 在原有基础上新增 `{metric_content}` 和 `{rag_examples}`：

```text
你是数据分析助手，请严格按下面的指标定义生成 Doris SQL。

【表结构】
{schema}

【匹配到的指标定义】
指标：订单量(order_count)
表：OrderTide
聚合：SUM(numbers)
时间维度：timeinfo
可用维度：productypeid

【历史相似问题】
问题：最近7天各产品类型订单量
SQL：SELECT timeinfo, productypeid, SUM(numbers) FROM OrderTide ...

【用户问题】
{question}

请返回 JSON：{sql, chartType, title, xAxis, yAxis, xAxisLabel, yAxisLabel}
```

---

## 6. 数据流

1. `POST /ai/query` 接收 `question`。
2. `MetricSemanticParser.parse(question)` 返回 `List<MatchedMetric>`。
3. `RagRetriever.retrieve(question, 3)` 返回 `List<QAExample>`。
4. `AiPromptBuilder.buildPrompt(question, metrics, examples)` 生成新 prompt。
5. `AiService` 调用 DeepSeek，解析 JSON 响应。
6. `AiSqlValidator.validate(sql)` 校验 SQL。
7. `DorisDBUtil` 执行 SQL。
8. 返回 `AiQueryResult`。
9. 成功时异步写入 `ai_qa_history`。

---

## 7. 错误处理

| 场景 | 处理 |
|---|---|
| 未命中任何指标 | 降级为原有直接 Text-to-SQL，记录日志 |
| 命中多个指标 | 取置信度最高项，响应带 `matchedMetric` 字段 |
| RAG 未召回示例 | 继续生成，prompt 不写历史示例 |
| SQL 校验失败 | 返回 `errorMessage + 原始 SQL`，不入历史库 |
| SQL 执行失败 | 记录完整 SQL 和异常，返回“查询执行失败” |
| 写入历史库失败 | 不阻塞返回，只打日志 |

---

## 8. 测试

### 8.1 单元测试

- `MetricSemanticParserTest`
  - “最近7天订单量” → 命中 `order_count`
  - “昨天 ABTest UV” → 命中 `abtest_uv`
  - “按渠道看营销成交” → 命中 `marketing_deal`
- `RagRetrieverTest`
  - 验证 MySQL 全文检索召回逻辑

### 8.2 集成测试

- `AiServiceIntegrationTest`
  - 端到端跑通一个指标查询，验证返回包含正确 SQL、列、行、图表配置。

---

## 9. 开源参考

- [Vanna AI](https://github.com/vanna-ai/vanna)：RAG-based Text-to-SQL，用 DDL + SQL 示例训练检索模型。
- [Wren AI](https://github.com/Canner/WrenAI)：语义模型 + RAG 混合，用 MDL 定义语义层。
- [dbt MetricFlow](https://github.com/dbt-labs/metricflow)：YAML 定义指标，确定性生成 SQL。
- [Cube](https://github.com/cube-js/cube)：AI/BI 语义层，支持 MCP server。
- [dbt Labs Benchmark](https://docs.getdbt.com/blog/semantic-layer-vs-text-to-sql-2026)：语义层 + Text-to-SQL 混合方案准确率更高。

---

## 10. 下一步

设计确认后，进入 `writing-plans` 阶段，输出详细实现计划与文件清单。
