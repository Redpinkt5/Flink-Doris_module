package cn.rp.service;

import cn.rp.result.TerminalAnalyResult;
import cn.rp.util.DateUtil;
import cn.rp.util.DorisDBUtil;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.util.*;

/**
 * 终端分析服务
 *
 * <p>
 * 根据 cn.rp.analy.terminal.sink 的输出表（TerminalLiuliang、TerminalNewUser）做趋势查询。
 * 字段：timeinfo、numbers、deviceType。
 * </p>
 *
 * @author Rp
 * @date 2026.6.24 23:50
 **/
@Service
public class TerminalService {

    private static final String URL = "jdbc:mysql://192.168.88.101:9030/hadoop_db";
    private static final String DRIVER = "com.mysql.cj.jdbc.Driver";
    private static final String USER = "hadoop";
    private static final String PWD = "hadoop";

    // ==================== 终端流量（TerminalLiuliang 表） ====================

    /**
     * 终端流量日趋势：按 deviceType 拆分
     */
    public TerminalAnalyResult terminalTrafficDayTide() {
        return deviceTide("TerminalLiuliang", 8, "yyyyMMdd", "终端流量");
    }

    /**
     * 终端流量小时趋势
     */
    public TerminalAnalyResult terminalTrafficHourTide() {
        return deviceTide("TerminalLiuliang", 10, "yyyyMMddHH", "终端流量");
    }

    // ==================== 用户趋势（TerminalNewUser 表） ====================

    /**
     * 用户趋势（新增用户）日
     */
    public TerminalAnalyResult terminalUserDayTide() {
        return deviceTide("TerminalNewUser", 8, "yyyyMMdd", "用户趋势");
    }

    /**
     * 用户趋势（新增用户）小时
     */
    public TerminalAnalyResult terminalUserHourTide() {
        return deviceTide("TerminalNewUser", 10, "yyyyMMddHH", "用户趋势");
    }

    // ==================== 公共实现 ====================

    /**
     * 终端维度趋势：按 deviceType 拆成多个序列
     */
    private TerminalAnalyResult deviceTide(String tableName, int timeLen, String pattern, String defaultName) {
        try {
            String sql = "select timeinfo, numbers, deviceType from (" +
                    "  select timeinfo, numbers, deviceType," +
                    "    row_number() over(partition by deviceType order by timeinfo desc) as rownumber" +
                    "  from " + tableName +
                    "  where length(timeinfo) = " + timeLen +
                    ") aa where rownumber <= 10;";
            ResultSet resultSet = DorisDBUtil.quereBySql(URL, DRIVER, USER, PWD, sql);

            // 外层：设备类型；内层：时间 -> numbers
            Map<String, Map<String, Long>> deviceMap = new LinkedHashMap<>();
            Set<String> timeSet = new TreeSet<>((o1, o2) -> DateUtil.compareDate(o1, o2, pattern));

            while (resultSet.next()) {
                String timeinfo = resultSet.getString("timeinfo");
                int deviceType = resultSet.getInt("deviceType");
                long numbers = resultSet.getLong("numbers");

                timeSet.add(timeinfo);
                String deviceName = deviceTypeName(deviceType);
                Map<String, Long> inner = deviceMap.computeIfAbsent(deviceName, k -> new LinkedHashMap<>());
                inner.put(timeinfo, numbers);
            }

            List<String> xList = new ArrayList<>(timeSet);

            List<Map<String, Object>> yData = new ArrayList<>();
            // 如果只有一种设备类型（没有拆分），用 defaultName
            if (deviceMap.size() <= 1 && !deviceMap.isEmpty()) {
                Map<String, Long> only = deviceMap.values().iterator().next();
                List<Long> dataList = new ArrayList<>();
                for (String timeinfo : xList) {
                    Long val = only.get(timeinfo);
                    dataList.add(val == null ? 0L : val);
                }
                Map<String, Object> yMap = new HashMap<>();
                yMap.put("name", defaultName);
                yMap.put("data", dataList);
                yData.add(yMap);
            } else {
                for (Map.Entry<String, Map<String, Long>> entry : deviceMap.entrySet()) {
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
            }

            TerminalAnalyResult result = new TerminalAnalyResult();
            result.setXList(xList);
            result.setYData(yData);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 设备类型 0/1/2 → 终端/移动/PC
     */
    private String deviceTypeName(int deviceType) {
        switch (deviceType) {
            case 1:
                return "移动端";
            case 2:
                return "PC端";
            case 0:
                return "其他";
            default:
                return "设备" + deviceType;
        }
    }
}
