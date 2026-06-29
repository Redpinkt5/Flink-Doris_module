package cn.rp.service;

import cn.rp.result.OrderAnalyResult;
import cn.rp.util.DateUtil;
import cn.rp.util.DorisDBUtil;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.util.*;

/**
 * 订单分析服务
 *
 * <p>
 * 根据 cn.rp.analy.orderAnaly.sink.OrderAnalySink 的输出表（OrderTide）做趋势查询。
 * 字段：timeinfo、numbers。
 * </p>
 *
 * @author Rp
 * @date 2026.6.24 23:50
 **/
@Service
public class OrderService {

    /**
     * 订单趋势 - 日
     */
    public OrderAnalyResult orderDayTide() {
        return totalTide("OrderTide", 8, "yyyyMMdd", "订单趋势");
    }

    /**
     * 订单趋势 - 小时
     */
    public OrderAnalyResult orderHourTide() {
        return totalTide("OrderTide", 10, "yyyyMMddHH", "订单趋势");
    }

    /**
     * 订单类目趋势 - 日
     *
     * <p>按 productTypeid 分组，统计每个类目每天的订单量。
     * xList 是时间，yData 是多条序列（每个类目一条）。</p>
     */
    public OrderAnalyResult orderCategoryDayTide() {
        return categoryTide("OrderTide", 8, "yyyyMMdd");
    }

    /**
     * 订单类目趋势 - 小时
     */
    public OrderAnalyResult orderCategoryHourTide() {
        return categoryTide("OrderTide", 10, "yyyyMMddHH");
    }

    // ==================== 公共实现 ====================

    /**
     * 订单总体趋势
     */
    private OrderAnalyResult totalTide(String tableName, int timeLen, String pattern, String seriesName) {
        try {
            String sql = "select timeinfo,numbers from " + tableName
                    + " where length(timeinfo) = " + timeLen
                    + " order by timeinfo desc limit 10;";

            ResultSet resultSet = DorisDBUtil.quereBySql(
                    "jdbc:mysql://192.168.88.101:9030/dorisTest",
                    "com.mysql.cj.jdbc.Driver", "root", "123456", sql);

            Map<String, Long> dataMap = new LinkedHashMap<>();
            while (resultSet.next()) {
                String timeinfo = resultSet.getString("timeinfo");
                long numbers = resultSet.getLong("numbers");
                dataMap.put(timeinfo, numbers);
            }

            List<String> sortList = new ArrayList<>(dataMap.keySet());
            Collections.sort(sortList, (o1, o2) -> DateUtil.compareDate(o1, o2, pattern));

            List<Long> dataList = new ArrayList<>();
            for (String timeinfo : sortList) {
                dataList.add(dataMap.get(timeinfo));
            }

            Map<String, Object> yMap = new HashMap<>();
            yMap.put("name", seriesName);
            yMap.put("data", dataList);

            List<Map<String, Object>> yData = new ArrayList<>();
            yData.add(yMap);

            OrderAnalyResult result = new OrderAnalyResult();
            result.setXList(sortList);
            result.setYData(yData);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 订单类目趋势：按 productTypeid 拆分多个序列
     */
    private OrderAnalyResult categoryTide(String tableName, int timeLen, String pattern) {
        try {
            String sql = "select timeinfo,productypeid,numbers from " + tableName
                    + " where length(timeinfo) = " + timeLen
                    + " order by timeinfo desc limit 100;";

            ResultSet resultSet = DorisDBUtil.quereBySql(
                    "jdbc:mysql://192.168.88.101:9030/dorisTest",
                    "com.mysql.cj.jdbc.Driver", "root", "123456", sql);

            // 外层：类目；内层：时间 -> numbers
            Map<String, Map<String, Long>> categoryMap = new LinkedHashMap<>();
            Set<String> timeSet = new TreeSet<>((o1, o2) -> DateUtil.compareDate(o1, o2, pattern));

            while (resultSet.next()) {
                String timeinfo = resultSet.getString("timeinfo");
                String category = resultSet.getString("productypeid");
                if (category == null || category.isEmpty()) {
                    category = "未知类目";
                }
                long numbers = resultSet.getLong("numbers");

                timeSet.add(timeinfo);
                Map<String, Long> inner = categoryMap.computeIfAbsent(category, k -> new LinkedHashMap<>());
                inner.put(timeinfo, numbers);
            }

            List<String> xList = new ArrayList<>(timeSet);

            List<Map<String, Object>> yData = new ArrayList<>();
            for (Map.Entry<String, Map<String, Long>> entry : categoryMap.entrySet()) {
                List<Long> dataList = new ArrayList<>();
                Map<String, Long> inner = entry.getValue();
                for (String timeinfo : xList) {
                    Long val = inner.get(timeinfo);
                    dataList.add(val == null ? 0L : val);
                }
                Map<String, Object> yMap = new HashMap<>();
                yMap.put("name", entry.getKey());
                yMap.put("data", dataList);
                yData.add(yMap);
            }

            OrderAnalyResult result = new OrderAnalyResult();
            result.setXList(xList);
            result.setYData(yData);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
