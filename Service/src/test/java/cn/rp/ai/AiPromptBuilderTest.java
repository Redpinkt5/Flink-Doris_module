package cn.rp.ai;

import cn.rp.ai.rag.QAExample;
import cn.rp.ai.rag.RagRetriever;
import cn.rp.ai.semantic.MetricDefinition;
import cn.rp.ai.semantic.MetricSemanticParser;
import cn.rp.config.AiSchemaConfig;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertTrue;

public class AiPromptBuilderTest {

    private AiPromptBuilder createBuilder() {
        AiPromptBuilder builder = new AiPromptBuilder();
        builder.setSchemaConfig(new AiSchemaConfig());
        return builder;
    }

    @Test
    public void buildSystemPrompt_shouldInjectMetricAndRagContent() {
        MetricDefinition metric = new MetricDefinition();
        metric.setName("order_count");
        metric.setDisplayName("订单量");
        metric.setTable("OrderTide");
        metric.setAggregation("SUM(numbers)");
        metric.setTimeDimension("timeinfo");
        metric.setDefaultTimeRange("7d");

        MetricSemanticParser parser = new MetricSemanticParser(Arrays.asList(metric));

        QAExample example = new QAExample();
        example.setQuestion("最近7天订单量");
        example.setGeneratedSql("SELECT timeinfo, SUM(numbers) FROM OrderTide");
        example.setChartType("line");

        RagRetriever retriever = new RagRetriever() {
            @Override
            public List<QAExample> retrieve(String question, int topK) {
                return Arrays.asList(example);
            }
        };

        AiPromptBuilder builder = createBuilder();
        builder.setMetricSemanticParser(parser);
        builder.setRagRetriever(retriever);

        String prompt = builder.buildSystemPrompt("最近7天订单量");
        assertTrue("prompt 应包含指标内容", prompt.contains("指标：订单量(order_count)"));
        assertTrue("prompt 应包含历史问题", prompt.contains("问题：最近7天订单量"));
        assertTrue("prompt 应包含历史 SQL", prompt.contains("SELECT timeinfo, SUM(numbers) FROM OrderTide"));
    }

    @Test
    public void buildSystemPrompt_shouldFallbackWhenDependenciesMissing() {
        AiPromptBuilder builder = createBuilder();
        builder.setMetricSemanticParser(null);
        builder.setRagRetriever(null);

        String prompt = builder.buildSystemPrompt("随便问个问题");
        assertTrue("无依赖时应返回基础 prompt", prompt.contains("## Schema"));
        assertTrue("无 metric 依赖时应显示降级提示", prompt.contains("（未命中任何预定义指标，请根据 schema 推断）"));
        assertTrue("无 rag 依赖时应显示降级提示", prompt.contains("（暂无历史相似问题）"));
    }
}
