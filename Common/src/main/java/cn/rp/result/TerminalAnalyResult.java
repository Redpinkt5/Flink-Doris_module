package cn.rp.result;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.*;


/**
 * @author Rp
 * @date 2026.6.12 14:45
 **/
public class AbTestResult {
    private List<String> xList = new ArrayList<String>();
    private List<Map<String, Object>> yData = new ArrayList<Map<String, Object>>();


    private List<Map<String, Object>> tableData = new ArrayList<Map<String, Object>>();

    private List<Map<String, Object>> tableData2 = new ArrayList<Map<String, Object>>();


    public AbTestResult() {
    }

    public AbTestResult(List<String> xList, List<Map<String, Object>> yData, List<Map<String, Object>> tableData, List<Map<String, Object>> tableData2) {
        this.xList = xList;
        this.yData = yData;
        this.tableData = tableData;
        this.tableData2 = tableData2;
    }

    /**
     * 获取
     * @return xList
     */
    @JsonProperty("xList")
    public List<String> getXList() {
        return xList;
    }

    /**
     * 设置
     * @param xList
     */
    public void setXList(List<String> xList) {
        this.xList = xList;
    }

    /**
     * 获取
     * @return yData
     */
    @JsonProperty("yData")
    public List<Map<String, Object>> getYData() {
        return yData;
    }

    /**
     * 设置
     * @param yData
     */
    public void setYData(List<Map<String, Object>> yData) {
        this.yData = yData;
    }

    /**
     * 获取
     * @return tableData
     */
    @JsonProperty("tableData")
    public List<Map<String, Object>> getTableData() {
        return tableData;
    }

    /**
     * 设置
     * @param tableData
     */
    public void setTableData(List<Map<String, Object>> tableData) {
        this.tableData = tableData;
    }

    /**
     * 获取
     * @return tableData2
     */
    @JsonProperty("tableData2")
    public List<Map<String, Object>> getTableData2() {
        return tableData2;
    }

    /**
     * 设置
     * @param tableData2
     */
    public void setTableData2(List<Map<String, Object>> tableData2) {
        this.tableData2 = tableData2;
    }

    public String toString() {
        return "AbTestResult{xList = " + xList + ", yData = " + yData + ", tableData = " + tableData + ", tableData2 = " + tableData2 + "}";
    }
}


