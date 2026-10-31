package cn.rp.ai.semantic;

/**
 * 问题与语义指标的匹配结果，按 score 降序后取 TopK 注入 Prompt。
 */
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
