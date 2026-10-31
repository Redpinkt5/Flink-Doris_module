package cn.rp.param;

/**
 * @author Rp
 * @date 2026.6.13 17:07
 **/
public class AbTestVo {
    private String timedate;
    private String versiontype;


    public AbTestVo() {
    }

    public AbTestVo(String timedate, String versiontype) {
        this.timedate = timedate;
        this.versiontype = versiontype;
    }

    /**
     * 获取
     * @return timedate
     */
    public String getTimedate() {
        return timedate;
    }

    /**
     * 设置
     * @param timedate
     */
    public void setTimedate(String timedate) {
        this.timedate = timedate;
    }

    /**
     * 获取
     * @return versiontype
     */
    public String getVersiontype() {
        return versiontype;
    }

    /**
     * 设置
     * @param versiontype
     */
    public void setVersiontype(String versiontype) {
        this.versiontype = versiontype;
    }

    public String toString() {
        return "AbTestVo{timedate = " + timedate + ", versiontype = " + versiontype + "}";
    }
}