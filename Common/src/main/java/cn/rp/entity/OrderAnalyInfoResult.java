package cn.rp.entity;

/**
 * @author Rp
 * @date 2026.6.17 20:46
 **/
public class TerminalInfoResult {
    private String timeInfo;
    private long numbers;
    private int deviceType;
    private String groupField;

    public TerminalInfoResult() {
    }

    public TerminalInfoResult(String timeInfo, long numbers, int deviceType, String groupField) {
        this.timeInfo = timeInfo;
        this.numbers = numbers;
        this.deviceType = deviceType;
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
     * @return deviceType
     */
    public int getDeviceType() {
        return deviceType;
    }

    /**
     * 设置
     * @param deviceType
     */
    public void setDeviceType(int deviceType) {
        this.deviceType = deviceType;
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
        return "TerminalInfoResult{timeInfo = " + timeInfo + ", numbers = " + numbers + ", deviceType = " + deviceType + ", groupField = " + groupField + "}";
    }
}