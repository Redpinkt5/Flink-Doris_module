package cn.rp.result;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * @author Rp
 * @date 2026.6.12 14:45
 **/
public class MarketingAnalyResult {
    private List<String> xList = new ArrayList<String>();
    private List<Map<String, Object>> yData = new ArrayList<Map<String, Object>>();


    public MarketingAnalyResult() {
    }

    public MarketingAnalyResult(List<String> xList, List<Map<String, Object>> yData) {
        this.xList = xList;
        this.yData = yData;
    }

    /**
     * 获取
     * @return xList
     */
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

    public String toString() {
        return "MarketingAnalyResult{xList = " + xList + ", yData = " + yData + "}";
    }
}


