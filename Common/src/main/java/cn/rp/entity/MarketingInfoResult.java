package cn.rp.entity;

/**
 * @author Rp
 * @date 2026.6.18 16:44
 * 团购分析
 * 秒杀分析
 * 大促活动分析
 *
 **/
public class MarketingInfoResult {
    private String timeinfo;
    private long numbers;
    private long tuangouid; //团购id
    private long miaoshaid; //秒杀id
    private long huodongid; //活动id
    private String groupFieldId;


    public MarketingInfoResult() {
    }

    public MarketingInfoResult(String timeinfo, long numbers, long tuangouid, long miaoshaid, long huodongid, String groupFieldId) {
        this.timeinfo = timeinfo;
        this.numbers = numbers;
        this.tuangouid = tuangouid;
        this.miaoshaid = miaoshaid;
        this.huodongid = huodongid;
        this.groupFieldId = groupFieldId;
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
     * @return tuangouid
     */
    public long getTuangouid() {
        return tuangouid;
    }

    /**
     * 设置
     * @param tuangouid
     */
    public void setTuangouid(long tuangouid) {
        this.tuangouid = tuangouid;
    }

    /**
     * 获取
     * @return miaoshaid
     */
    public long getMiaoshaid() {
        return miaoshaid;
    }

    /**
     * 设置
     * @param miaoshaid
     */
    public void setMiaoshaid(long miaoshaid) {
        this.miaoshaid = miaoshaid;
    }

    /**
     * 获取
     * @return huodongid
     */
    public long getHuodongid() {
        return huodongid;
    }

    /**
     * 设置
     * @param huodongid
     */
    public void setHuodongid(long huodongid) {
        this.huodongid = huodongid;
    }

    /**
     * 获取
     * @return groupFieldId
     */
    public String getGroupFieldId() {
        return groupFieldId;
    }

    /**
     * 设置
     * @param groupFieldId
     */
    public void setGroupFieldId(String groupFieldId) {
        this.groupFieldId = groupFieldId;
    }

    public String toString() {
        return "MarketingInfoResult{timeinfo = " + timeinfo + ", numbers = " + numbers + ", tuangouid = " + tuangouid + ", miaoshaid = " + miaoshaid + ", huodongid = " + huodongid + ", groupFieldId = " + groupFieldId + "}";
    }
}