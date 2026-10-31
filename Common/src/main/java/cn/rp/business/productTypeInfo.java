package cn.rp.business;

/**
 * @author Rp
 * @date 2026.6.16 17:53
 **/
public class productTypeInfo {
    private long id;
    private String producttypeName;
    private int productTypeleave;
    private long parentId;

    public productTypeInfo() {
    }

    public productTypeInfo(long id, String producttypeName, int productTypeleave, long parentId) {
        this.id = id;
        this.producttypeName = producttypeName;
        this.productTypeleave = productTypeleave;
        this.parentId = parentId;
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
     * @return producttypeName
     */
    public String getProducttypeName() {
        return producttypeName;
    }

    /**
     * 设置
     * @param producttypeName
     */
    public void setProducttypeName(String producttypeName) {
        this.producttypeName = producttypeName;
    }

    /**
     * 获取
     * @return productTypeleave
     */
    public int getProductTypeleave() {
        return productTypeleave;
    }

    /**
     * 设置
     * @param productTypeleave
     */
    public void setProductTypeleave(int productTypeleave) {
        this.productTypeleave = productTypeleave;
    }

    /**
     * 获取
     * @return parentId
     */
    public long getParentId() {
        return parentId;
    }

    /**
     * 设置
     * @param parentId
     */
    public void setParentId(long parentId) {
        this.parentId = parentId;
    }

    public String toString() {
        return "productTypeInfo{id = " + id + ", producttypeName = " + producttypeName + ", productTypeleave = " + productTypeleave + ", parentId = " + parentId + "}";
    }
}