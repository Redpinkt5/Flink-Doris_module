package cn.rp.ai;

import cn.rp.ai.semantic.MetricDefinition;
import cn.rp.ai.semantic.MetricMatchResult;
import cn.rp.ai.semantic.MetricSemanticParser;
import cn.rp.config.AiSchemaConfig;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertTrue;

public class AiPromptBuilderTest {

    private AiPromptBuilder createBuilder() {
        AiPromptBuilder builder = new AiPromptBuilder();
        builder.setSchemaConfig(new AiSchemaConfig());
        return builder;
    }

    @Test
    public void buildSystemPrompt_shouldInjectMetricContent() {
        MetricDefinition metric = new MetricDefinition();
        metric.setName("order_count");
        metric.setDisplayName("订单量");
        metric.setTable("OrderTide");
        metric.setAggregation("SUM(numbers)");
        metric.setTimeDimension("timeinfo");
        metric.setDefaultTimeRange("7d");

        MetricSemanticParser parser = new MetricSemanticParser(Arrays.asList(metric));

        AiPromptBuilder builder = createBuilder();
        builder.setMetricSemanticParser(parser);

        String prompt = builder.buildSystemPrompt("最近7天订单量");
        assertTrue("prompt 应包含指标内容", prompt.contains("指标：订单量(order_count)"));
    }

    @Test
    public void buildSystemPrompt_withListOverload_shouldInjectProvidedMatches() {
        MetricDefinition metric = new MetricDefinition();
        metric.setName("order_count");
        metric.setDisplayName("订单量");
        metric.setTable("OrderTide");
        metric.setAggregation("SUM(numbers)");
        metric.setTimeDimension("timeinfo");
        metric.setDefaultTimeRange("7d");

        MetricSemanticParser parser = new MetricSemanticParser(Arrays.asList(metric));

        AiPromptBuilder builder = createBuilder();
        builder.setMetricSemanticParser(parser);

        List<MetricMatchResult> matches = parser.parse("最近7天订单量");

        String prompt = builder.buildSystemPrompt(matches);
        assertTrue("prompt 应包含指标内容", prompt.contains("指标：订单量(order_count)"));
    }

    @Test
    public void buildSystemPrompt_shouldUsePartialDependencyWhenMetricAvailable() {
        MetricDefinition metric = new MetricDefinition();
        metric.setName("order_count");
        metric.setDisplayName("订单量");
        metric.setTable("OrderTide");
        metric.setAggregation("SUM(numbers)");
        metric.setTimeDimension("timeinfo");
        metric.setDefaultTimeRange("7d");

        MetricSemanticParser parser = new MetricSemanticParser(Arrays.asList(metric));

        AiPromptBuilder builder = createBuilder();
        builder.setMetricSemanticParser(parser);

        String prompt = builder.buildSystemPrompt("最近7天订单量");
        assertTrue("有 metric 依赖时应注入指标内容", prompt.contains("指标：订单量(order_count)"));
    }

    @Test
    public void buildSystemPrompt_shouldFallbackWhenDependenciesMissing() {
        AiPromptBuilder builder = createBuilder();
        builder.setMetricSemanticParser(null);

        String prompt = builder.buildSystemPrompt("随便问个问题");
        assertTrue("无依赖时应返回基础 prompt", prompt.contains("## Schema"));
        assertTrue("无 metric 依赖时应显示降级提示", prompt.contains("（未命中任何预定义指标，请根据 schema 推断）"));
    }
}
