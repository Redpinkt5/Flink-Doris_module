package cn.rp.business;

/**
 * @author Rp
 * @date 2026.6.16 17:52
 **/
public class productInfo {
    private long id;
    private String productTypeid;
    private double originalPrice;
    private long shopid;

    public productInfo() {
    }

    public productInfo(long id, String productTypeid, double originalPrice, long shopid) {
        this.id = id;
        this.productTypeid = productTypeid;
        this.originalPrice = originalPrice;
        this.shopid = shopid;
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
     * @return productTypeid
     */
    public String getProductTypeid() {
        return productTypeid;
    }

    /**
     * 设置
     * @param productTypeid
     */
    public void setProductTypeid(String productTypeid) {
        this.productTypeid = productTypeid;
    }

    /**
     * 获取
     * @return originalPrice
     */
    public double getOriginalPrice() {
        return originalPrice;
    }

    /**
     * 设置
     * @param originalPrice
     */
    public void setOriginalPrice(double originalPrice) {
        this.originalPrice = originalPrice;
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

    public String toString() {
        return "productInfo{id = " + id + ", productTypeid = " + productTypeid + ", originalPrice = " + originalPrice + ", shopid = " + shopid + "}";
    }
}