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
