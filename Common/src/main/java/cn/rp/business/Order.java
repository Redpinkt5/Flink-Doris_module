package cn.rp.business;

import com.sun.org.apache.xml.internal.utils.ObjectVector;

import java.util.Date;
import java.util.Objects;

/**
 * @author Rp
 * @date 2026.6.16 17:53
 **/
public class Order {
    private long id;
    private long userid;
    private long productid;
    private long shopid;
    private double orderAmount;
    private long merchartid;
    private double payAmount;
    private Date createTime;
    private int payType;//1微信支付 2支付宝 3银联 4京东
    private Date payTime;
    private int status;
    private long number;
    private long huodongid;
    private long couponId;
    private double couponAmount;
    private long miaoshaid;
    private long tuangouid;


    public Order() {
    }

    public Order(long id, long userid, long productid, long shopid, double orderAmount, long merchartid, double payAmount, Date createTime, int payType, Date payTime, int status, long number, long huodongid, long couponId, double couponAmount, long miaoshaid, long tuangouid) {
        this.id = id;
        this.userid = userid;
        this.productid = productid;
        this.shopid = shopid;
        this.orderAmount = orderAmount;
        this.merchartid = merchartid;
        this.payAmount = payAmount;
        this.createTime = createTime;
        this.payType = payType;
        this.payTime = payTime;
        this.status = status;
        this.number = number;
        this.huodongid = huodongid;
        this.couponId = couponId;
        this.couponAmount = couponAmount;
        this.miaoshaid = miaoshaid;
        this.tuangouid = tuangouid;
    }

    /**
     * 获取
     * @return id
     */
    public long getId() {
        return id;
    }

    /**
     * 设置
     * @param id
     */
    public void setId(long id) {
        this.id = id;
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
     * @return productid
     */
    public long getProductid() {
        return productid;
    }

    /**
     * 设置
     * @param productid
     */
    public void setProductid(long productid) {
        this.productid = productid;
    }

    /**
     * 获取
     * @return shopid
     */
    public long getShopid() {
        return shopid;
    }

    /**
     * 设置
     * @param shopid
     */
    public void setShopid(long shopid) {
        this.shopid = shopid;
    }

    /**
     * 获取
     * @return orderAmount
     */
    public double getOrderAmount() {
        return orderAmount;
    }

    /**
     * 设置
     * @param orderAmount
     */
    public void setOrderAmount(double orderAmount) {
        this.orderAmount = orderAmount;
    }

    /**
     * 获取
     * @return merchartid
     */
    public long getMerchartid() {
        return merchartid;
    }

    /**
     * 设置
     * @param merchartid
     */
    public void setMerchartid(long merchartid) {
        this.merchartid = merchartid;
    }

    /**
     * 获取
     * @return payAmount
     */
    public double getPayAmount() {
        return payAmount;
    }

    /**
     * 设置
     * @param payAmount
     */
    public void setPayAmount(double payAmount) {
        this.payAmount = payAmount;
    }

    /**
     * 获取
     * @return createTime
     */
    public Date getCreateTime() {
        return createTime;
    }

    /**
     * 设置
     * @param createTime
     */
    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    /**
     * 获取
     * @return payType
     */
    public int getPayType() {
        return payType;
    }

    /**
     * 设置
     * @param payType
     */
    public void setPayType(int payType) {
        this.payType = payType;
    }

    /**
     * 获取
     * @return payTime
     */
    public Date getPayTime() {
        return payTime;
    }

    /**
     * 设置
     * @param payTime
     */
    public void setPayTime(Date payTime) {
        this.payTime = payTime;
    }

    /**
     * 获取
     * @return status
     */
    public int getStatus() {
        return status;
    }

    /**
     * 设置
     * @param status
     */
    public void setStatus(int status) {
        this.status = status;
    }

    /**
     * 获取
     * @return number
     */
    public long getNumber() {
        return number;
    }

    /**
     * 设置
     * @param number
     */
    public void setNumber(long number) {
        this.number = number;
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
     * @return couponId
     */
    public long getCouponId() {
        return couponId;
    }

    /**
     * 设置
     * @param couponId
     */
    public void setCouponId(long couponId) {
        this.couponId = couponId;
    }

    /**
     * 获取
     * @return couponAmount
     */
    public double getCouponAmount() {
        return couponAmount;
    }

    /**
     * 设置
     * @param couponAmount
     */
    public void setCouponAmount(double couponAmount) {
        this.couponAmount = couponAmount;
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

    public String toString() {
        return "Order{id = " + id + ", userid = " + userid + ", productid = " + productid + ", shopid = " + shopid + ", orderAmount = " + orderAmount + ", merchartid = " + merchartid + ", payAmount = " + payAmount + ", createTime = " + createTime + ", payType = " + payType + ", payTime = " + payTime + ", status = " + status + ", number = " + number + ", huodongid = " + huodongid + ", couponId = " + couponId + ", couponAmount = " + couponAmount + ", miaoshaid = " + miaoshaid + ", tuangouid = " + tuangouid + "}";
    }
    
}