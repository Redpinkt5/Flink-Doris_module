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
