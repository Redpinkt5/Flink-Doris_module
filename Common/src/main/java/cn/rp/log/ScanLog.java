package cn.rp.log;

/**
 * @author Rp
 * @date 2026.5.28 18:55
 * 用户浏览日志
 **/
public class ScanLog {
    private long userid;
    private long proucntid;
    private long productypeid;
    private long productypeleeave;
    private long productyParentid;
    private long advertisingid;
    private long time;
    private long tuangouid; //团购id
    private long miaoshaid; //秒杀id
    private long huodongid; //活动id
    private int testFlag; //测试标志(AB基本)
    private int dievceType; //设备类型


    public ScanLog() {
    }

    public ScanLog(long userid, long proucntid, long productypeid, long productypeleeave, long productyParentid, long advertisingid, long time, long tuangouid, long miaoshaid, long huodongid, int testFlag, int dievceType) {
        this.userid = userid;
        this.proucntid = proucntid;
        this.productypeid = productypeid;
        this.productypeleeave = productypeleeave;
        this.productyParentid = productyParentid;
        this.advertisingid = advertisingid;
        this.time = time;
        this.tuangouid = tuangouid;
        this.miaoshaid = miaoshaid;
        this.huodongid = huodongid;
        this.testFlag = testFlag;
        this.dievceType = dievceType;
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
     * @return proucntid
     */
    public long getProucntid() {
        return proucntid;
    }

    /**
     * 设置
     * @param proucntid
     */
    public void setProucntid(long proucntid) {
        this.proucntid = proucntid;
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
     * @return productyParentid
     */
    public long getProductyParentid() {
        return productyParentid;
    }

    /**
     * 设置
     * @param productyParentid
     */
    public void setProductyParentid(long productyParentid) {
        this.productyParentid = productyParentid;
    }

    /**
     * 获取
     * @return advertisingid
     */
    public long getAdvertisingid() {
        return advertisingid;
    }

    /**
     * 设置
     * @param advertisingid
     */
    public void setAdvertisingid(long advertisingid) {
        this.advertisingid = advertisingid;
    }

    /**
     * 获取
     * @return time
     */
    public long getTime() {
        return time;
    }

    /**
     * 设置
     * @param time
     */
    public void setTime(long time) {
        this.time = time;
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
     * @return testFlag
     */
    public int getTestFlag() {
        return testFlag;
    }

    /**
     * 设置
     * @param testFlag
     */
    public void setTestFlag(int testFlag) {
        this.testFlag = testFlag;
    }

    /**
     * 获取
     * @return dievceType
     */
    public int getDievceType() {
        return dievceType;
    }

    /**
     * 设置
     * @param dievceType
     */
    public void setDievceType(int dievceType) {
        this.dievceType = dievceType;
    }

    public String toString() {
        return "ScanLog{userid = " + userid + ", proucntid = " + proucntid + ", productypeid = " + productypeid + ", productypeleeave = " + productypeleeave + ", productyParentid = " + productyParentid + ", advertisingid = " + advertisingid + ", time = " + time + ", tuangouid = " + tuangouid + ", miaoshaid = " + miaoshaid + ", huodongid = " + huodongid + ", testFlag = " + testFlag + ", dievceType = " + dievceType + "}";
    }
}