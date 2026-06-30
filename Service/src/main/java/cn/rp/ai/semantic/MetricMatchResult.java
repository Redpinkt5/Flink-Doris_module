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
