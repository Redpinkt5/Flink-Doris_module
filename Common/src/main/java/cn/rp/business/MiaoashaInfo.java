package cn.rp.business;

/**
 * @author Rp
 * @date 2026.6.23 21:07
 **/
public class MiaoashaInfo {
    private int id;
    private String name;
    private String desc;


    public MiaoashaInfo() {
    }

    public MiaoashaInfo(int id, String name, String desc) {
        this.id = id;
        this.name = name;
        this.desc = desc;
    }

    /**
     * 获取
     * @return id
     */
    public int getId() {
        return id;
    }

    /**
     * 设置
     * @param id
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * 获取
     * @return name
     */
    public String getName() {
        return name;
    }

    /**
     * 设置
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * 获取
     * @return desc
     */
    public String getDesc() {
        return desc;
    }

    /**
     * 设置
     * @param desc
     */
    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String toString() {
        return "MiaoashaInfo{id = " + id + ", name = " + name + ", desc = " + desc + "}";
    }
}