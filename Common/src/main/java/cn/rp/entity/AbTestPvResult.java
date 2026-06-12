package cn.rp.entity;

/**
 * @author Rp
 * @date 2026.6.9 17:48
 **/
public class AbTestPvResult {
    private String timeinfo;
    private long numbers;
    private long productypeid;
    private long userid;
    private long productypeleeave;//用户id
    private String groupField;//分组条件

    public AbTestPvResult() {
    }

    public AbTestPvResult(String timeinfo, long numbers, long productypeid, long userid, long productypeleeave, String groupField) {
        this.timeinfo = timeinfo;
        this.numbers = numbers;
        this.productypeid = productypeid;
        this.userid = userid;
        this.productypeleeave = productypeleeave;
        this.groupField = groupField;
    }

    /**
     * 获取
     * @return timeinfo
     */
    public String getTimeinfo() {
        return timeinfo;
    }

    /**
     * 设置
     * @param timeinfo
     */
    public void setTimeinfo(String timeinfo) {
        this.timeinfo = timeinfo;
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
     * @return userid
     */
    public long getUserid() {
        return userid;
    }

    /**
     * 设置
     * @param userid
     */
    public void setUserid(long userid) {
        this.userid = userid;
    }

    /**
     * 获取
     * @return productypeleeave
     */
    public long getProductypeleeave() {
        return productypeleeave;
    }

    /**
     * 设置
     * @param productypeleeave
     */
    public void setProductypeleeave(long productypeleeave) {
        this.productypeleeave = productypeleeave;
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
        return "AbTestPvResult{timeinfo = " + timeinfo + ", numbers = " + numbers + ", productypeid = " + productypeid + ", userid = " + userid + ", productypeleeave = " + productypeleeave + ", groupField = " + groupField + "}";
    }
}