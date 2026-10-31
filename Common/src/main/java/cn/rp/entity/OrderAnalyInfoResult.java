package cn.rp.entity;

/**
 * @author Rp
 * @date 2026.6.17 20:46
 * 订单趋势
 * 订单类目趋势
 **/
public class OrderAnalyInfoResult {
    private String timeInfo;
    private long numbers;
    private long productypeid;
    private String groupField;

    public OrderAnalyInfoResult() {
    }

    public OrderAnalyInfoResult(String timeInfo, long numbers, long productypeid, String groupField) {
        this.timeInfo = timeInfo;
        this.numbers = numbers;
        this.productypeid = productypeid;
        this.groupField = groupField;
    }

    /**
     * 获取
     * @return timeInfo
     */
    public String getTimeInfo() {
        return timeInfo;
    }

    /**
     * 设置
     * @param timeInfo
     */
    public void setTimeInfo(String timeInfo) {
        this.timeInfo = timeInfo;
    }

    /**
     * 获取
     * @return numbers
     */
    public long getNumbers() {
        return numbers;
    }

    /**
     * 设置
     * @param numbers
     */
    public void setNumbers(long numbers) {
        this.numbers = numbers;
    }

    /**
     * 获取
     * @return productypeid
     */
    public long getProductypeid() {
        return productypeid;
    }

    /**
     * 设置
     * @param productypeid
     */
    public void setProductypeid(long productypeid) {
        this.productypeid = productypeid;
    }

    /**
     * 获取
     * @return groupField
     */
    public String getGroupField() {
        return groupField;
    }

    /**
     * 设置
     * @param groupField
     */
    public void setGroupField(String groupField) {
        this.groupField = groupField;
    }

    public String toString() {
        return "OrderAnalyInfoResult{timeInfo = " + timeInfo + ", numbers = " + numbers + ", productypeid = " + productypeid + ", groupField = " + groupField + "}";
    }
//    private long tuangouid;
//    private long miaoshaid;
//    private long hongbaoid;

}