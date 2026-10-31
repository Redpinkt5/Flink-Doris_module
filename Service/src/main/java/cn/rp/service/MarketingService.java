package cn.rp.service;

import cn.rp.result.MarketingAnalyResult;
import cn.rp.util.DateUtil;
import cn.rp.util.DorisDBUtil;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.util.*;

/**
 * 营销分析相关统计服务
 *
 * <p>
 * 提供团购、秒杀、大促活动三种营销方式，在流量（浏览）和交易（成交）两个维度，
 * 天级、小时级的趋势查询。
 * </p>
 *
 * @author Rp
 * @date 2026.6.24 09:00
 **/
@Service
public class MarketingService {

    // ==================== 1. 流量（浏览）趋势 ====================

    /**
     * 团购流量日趋势
     */
    public MarketingAnalyResult tuangouLiuliangDayTide() {
        return totalTide("MarketingLiuliangtuangou", 8, "yyyyMMdd", "团购流量");
    }

    /**
     * 团购流量小时趋势
     */
    public MarketingAnalyResult tuangouLiuliangHourTide() {
        return totalTide("MarketingLiuliangtuangou", 10, "yyyyMMddHH", "团购流量");
    }

    /**
     * 秒杀流量日趋势
     */
    public MarketingAnalyResult miaoshaLiuliangDayTide() {
        return totalTide("MarketingLiuliangmiaosha", 8, "yyyyMMdd", "秒杀流量");
    }

    /**
     * 秒杀流量小时趋势
     */
    public MarketingAnalyResult miaoshaLiuliangHourTide() {
        return totalTide("MarketingLiuliangmiaosha", 10, "yyyyMMddHH", "秒杀流量");
    }

    /**
     * 大促活动流量日趋势
     */
    public MarketingAnalyResult huodongLiuliangDayTide() {
        return totalTide("MarketingLiulianghuodong", 8, "yyyyMMdd", "大促活动流量");
    }

    /**
     * 大促活动流量小时趋势
     */
    public MarketingAnalyResult huodongLiuliangHourTide() {
        return totalTide("MarketingLiulianghuodong", 10, "yyyyMMddHH", "大促活动流量");
    }

    // ==================== 2. 交易（成交）趋势 ====================

    /**
     * 团购交易日趋势
     */
    public MarketingAnalyResult tuangouDealDayTide() {
        return totalTide("MarketingDealtuangou", 8, "yyyyMMdd", "团购交易");
    }

    /**
     * 团购交易小时趋势
     */
    public MarketingAnalyResult tuangouDealHourTide() {
        return totalTide("MarketingDealtuangou", 10, "yyyyMMddHH", "团购交易");
    }

    /**
     * 秒杀交易日趋势
     */
    public MarketingAnalyResult miaoshaDealDayTide() {
        return totalTide("MarketingDealmiaosha", 8, "yyyyMMdd", "秒杀交易");
    }

    /**
     * 秒杀交易小时趋势
     */
    public MarketingAnalyResult miaoshaDealHourTide() {
        return totalTide("MarketingDealmiaosha", 10, "yyyyMMddHH", "秒杀交易");
    }

    /**
     * 大促活动交易日趋势
     */
    public MarketingAnalyResult huodongDealDayTide() {
        return totalTide("MarketingDealhuodong", 8, "yyyyMMdd", "大促活动交易");
    }

    /**
     * 大促活动交易小时趋势
     */
    public MarketingAnalyResult huodongDealHourTide() {
        return totalTide("MarketingDealhuodong", 10, "yyyyMMddHH", "大促活动交易");
    }

    // ==================== 3. 兼容 Controller 已有接口名的快捷方法 ====================

    /** 对应 Controller: /tuangouLiuliang */
    public MarketingAnalyResult tuangouLiuliang() {
        return tuangouLiuliangDayTide();
    }

    /** 对应 Controller: /miaoshaDeal */
    public MarketingAnalyResult miaoshaDeal() {
        return miaoshaDealDayTide();
    }

    /** 对应 Controller: /huodongTrend */
    public MarketingAnalyResult huodongTrend() {
        return huodongLiuliangDayTide();
    }

    /** 对应 Controller: /dealTrend */
    public MarketingAnalyResult dealTrend() {
        return huodongDealDayTide();
    }

    // ==================== 4. 公共实现 ====================

    /**
     * 营销趋势查询公共方法
     *
     * <p>营销数据没有 testFlag 维度，只有 timeinfo + numbers。</p>
     *
     * @param tableName  Doris 表名
     * @param timeLen    timeinfo 长度：天 8，小时 10
     * @param pattern    时间排序格式
     * @param seriesName 返回 yData 中数据序列的中文名
     * @return xList：时间；yData：[{name, data}]
     */
    private MarketingAnalyResult totalTide(String tableName, int timeLen, String pattern, String seriesName) {
        try {
            String sql = "select timeinfo,numbers from " + tableName + " where length(timeinfo) = " + timeLen + " order by timeinfo desc limit 10;";

            ResultSet resultSet = DorisDBUtil.quereBySql("jdbc:mysql://192.168.88.101:9030/hadoop_db", "com.mysql.cj.jdbc.Driver", "hadoop", "hadoop", sql);

            // 用 LinkedHashMap 保持插入顺序，再统一按时间排序
            Map<String, Long> dataMap = new LinkedHashMap<>();
            while (resultSet.next()) {
                String timeinfoString = resultSet.getString("timeinfo");
                long numbers = resultSet.getLong("numbers");
                dataMap.put(timeinfoString, numbers);
            }

            // 按时间正序排列
            List<String> sortList = new ArrayList<>(dataMap.keySet());
            Collections.sort(sortList, new Comparator<String>() {
                @Override
                public int compare(String o1, String o2) {
                    return DateUtil.compareDate(o1, o2, pattern);
                }
            });

            List<Long> dataList = new ArrayList<>();
            for (String timeinfo : sortList) {
                dataList.add(dataMap.get(timeinfo));
            }

            Map<String, Object> yMap = new HashMap<>();
            yMap.put("name", seriesName);
            yMap.put("data", dataList);

            List<Map<String, Object>> yData = new ArrayList<>();
            yData.add(yMap);

            MarketingAnalyResult result = new MarketingAnalyResult();
            result.setXList(sortList);
            result.setYData(yData);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
