package cn.rp.service;

import cn.rp.param.AbTestVo;
import cn.rp.result.AbTestResult;
import cn.rp.util.DateUtil;
import cn.rp.util.DorisDBUtil;
import cn.rp.util.HbaseUtil2;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.util.*;

/**
 * AB测试相关统计服务
 *
 * <p>
 * 提供 PV/UV 在天级、小时级维度的总体趋势以及明细（按产品等级、按产品类目）查询。
 * </p>
 *
 * @author Rp
 * @date 2026.6.21 23:02
 **/
@Service
public class AbTestService {

    /**
     * testFlag 与中文版本名的映射
     */
    private static Map<Integer, String> testFlagMap = new HashMap<Integer, String>();

    /**
     * 产品等级编码与中文名称的映射
     *
     * <p>数据库字段叫 productypeleeave，但业务含义是产品等级，暂时用此映射。</p>
     */
    private static Map<Integer, String> productLeaveMap = new HashMap<Integer, String>();

    static {
        testFlagMap.put(1, "A版");
        testFlagMap.put(2, "B版");
        testFlagMap.put(3, "基本版");

        productLeaveMap.put(1, "等级1");
        productLeaveMap.put(2, "等级2");
        productLeaveMap.put(3, "等级3");
    }

    // ==================== 1. 总体趋势 ====================

    /**
     * PV 总体日趋势
     *
     * <p>按天聚合 AbTestPvTotal 表，返回每个版本最近 10 天的 PV 趋势。
     * xList 为日期，yData 为各版本数据序列。</p>
     */
    public AbTestResult pvTotalDayTide() {
        return totalDayTide("AbTestPvTotal");
    }

    /**
     * UV 总体日趋势
     *
     * <p>逻辑同 pvTotalDayTide，只是读取 AbTestUvTotal 表。</p>
     */
    public AbTestResult uvTotalDayTide() {
        return totalDayTide("AbTestUvTotal");
    }

    /**
     * PV 总体小时趋势
     *
     * <p>按小时聚合 AbTestPvTotal 表，返回最近 10 个小时的趋势。</p>
     */
    public AbTestResult pvTotalHourTide() {
        return totalHourTide("AbTestPvTotal");
    }

    /**
     * UV 总体小时趋势
     *
     * <p>按小时聚合 AbTestUvTotal 表。</p>
     */
    public AbTestResult uvTotalHourTide() {
        return totalHourTide("AbTestUvTotal");
    }

    // ==================== 2. PV 明细（天 / 小时） ====================

    /**
     * PV 按天明细（产品等级 + 产品类目）
     *
     * @param abTestVo 查询参数：timedate（天 yyyyMMdd）、versiontype（1/2/3）
     * @return tableData：产品等级分布；tableData2：产品类目分布
     */
    public AbTestResult pvDayDetail(AbTestVo abTestVo) {
        return pvDetail(abTestVo);
    }

    /**
     * PV 按小时明细（产品等级 + 产品类目）
     *
     * @param abTestVo 查询参数：timedate（小时 yyyyMMddHH）、versiontype（1/2/3）
     * @return tableData：产品等级分布；tableData2：产品类目分布
     */
    public AbTestResult pvHourDetail(AbTestVo abTestVo) {
        return pvDetail(abTestVo);
    }

    /**
     * PV 按天/小时 —— 只查产品等级分布
     */
    public AbTestResult pvDayproductLeave(AbTestVo abTestVo) {
        return buildGradeResult(abTestVo, "AbTestPv");
    }

    public AbTestResult pvHourproductLeave(AbTestVo abTestVo) {
        return buildGradeResult(abTestVo, "AbTestPv");
    }

    /**
     * PV 按天/小时 —— 只查产品类目分布
     */
    public AbTestResult pvDayproductType(AbTestVo abTestVo) {
        return buildTypeResult(abTestVo, "AbTestPv");
    }

    public AbTestResult pvHourproductType(AbTestVo abTestVo) {
        return buildTypeResult(abTestVo, "AbTestPv");
    }

    // ==================== 3. UV 明细（天 / 小时） ====================

    /**
     * UV 按天明细（产品等级 + 产品类目）
     */
    public AbTestResult uvDayDetail(AbTestVo abTestVo) {
        return uvDetail(abTestVo);
    }

    /**
     * UV 按小时明细（产品等级 + 产品类目）
     */
    public AbTestResult uvHourDetail(AbTestVo abTestVo) {
        return uvDetail(abTestVo);
    }

    /**
     * UV 按天/小时 —— 只查产品等级分布
     */
    public AbTestResult uvDayproductLeave(AbTestVo abTestVo) {
        return buildGradeResult(abTestVo, "AbTestUv");
    }

    public AbTestResult uvHourproductLeave(AbTestVo abTestVo) {
        return buildGradeResult(abTestVo, "AbTestUv");
    }

    /**
     * UV 按天/小时 —— 只查产品类目分布
     */
    public AbTestResult uvDayproductType(AbTestVo abTestVo) {
        return buildTypeResult(abTestVo, "AbTestUv");
    }

    public AbTestResult uvHourproductType(AbTestVo abTestVo) {
        return buildTypeResult(abTestVo, "AbTestUv");
    }

    // ==================== 4. 公共方法 ====================

    /**
     * 天级总体趋势公共方法
     *
     * @param tableName Doris 表名
     * @return xList: 日期；yData: 各版本数据
     */
    private AbTestResult totalDayTide(String tableName) {
        return totalTide(tableName, 8, "yyyyMMdd");
    }

    /**
     * 小时级总体趋势公共方法
     *
     * @param tableName Doris 表名
     * @return xList: 小时（yyyyMMddHH）；yData: 各版本数据
     */
    private AbTestResult totalHourTide(String tableName) {
        return totalTide(tableName, 10, "yyyyMMddHH");
    }

    /**
     * 总体趋势公共实现
     *
     * @param tableName Doris 表名
     * @param timeLen   timeinfo 长度：天 8，小时 10
     * @param pattern   排序日期格式
     * @return AbTestResult
     */
    private AbTestResult totalTide(String tableName, int timeLen, String pattern) {
        try {
            List<Map<String, Object>> ydata = new ArrayList<>();
            String sql = "select timeinfo,numbers,testFlag from " + tableName + " where length(timeinfo) = " + timeLen + " order by timeinfo desc limit 10;";

            ResultSet resultSet = DorisDBUtil.quereBySql("jdbc:mysql://192.168.88.101:9030/dorisTest", "com.mysql.cj.jdbc.Driver", "root", "123456", sql);
            HashSet<String> dataSet = new HashSet<>();
            // 外层 key：版本名；内层 key：时间；value：numbers 累加值
            Map<String, Map<String, Long>> mapVersionMap = new HashMap<String, Map<String, Long>>();
            while (resultSet.next()) {
                String timeinfoString = resultSet.getString("timeinfo");
                int testFlag = resultSet.getInt("testFlag");
                dataSet.add(timeinfoString);
                long numbers = resultSet.getLong("numbers");
                String flagWord = testFlagMap.get(testFlag);
                Map<String, Long> innerMap = mapVersionMap.get(flagWord);
                if (innerMap == null) {
                    innerMap = new HashMap<String, Long>();
                }
                // 同一版本、同一时间点多条数据需要累加
                long preData = innerMap.get(timeinfoString) == null ? 0L : innerMap.get(timeinfoString);
                innerMap.put(timeinfoString, preData + numbers);
                mapVersionMap.put(flagWord, innerMap);
            }

            List<String> sortList = new ArrayList<>(dataSet);
            Collections.sort(sortList, new Comparator<String>() {
                @Override
                public int compare(String o1, String o2) {
                    return DateUtil.compareDate(o1, o2, pattern);
                }
            });

            // 生成时间占位模板，保证每个版本缺失的时间点补 0
            Map<String, Long> dateMapTemp = new HashMap<String, Long>();
            for (String timeinfo : sortList) {
                dateMapTemp.put(timeinfo, 0L);
            }

            for (Map.Entry<String, Map<String, Long>> entry : mapVersionMap.entrySet()) {
                String name = entry.getKey();
                Map<String, Long> dataMap = entry.getValue();
                Map<String, Long> deepCopy = new HashMap<String, Long>();
                deepCopy.putAll(dateMapTemp);
                for (Map.Entry<String, Long> e : dataMap.entrySet()) {
                    deepCopy.put(e.getKey(), e.getValue());
                }
                List<Object> dataList = new ArrayList<>();
                for (String key : sortList) {
                    dataList.add(deepCopy.get(key));
                }
                Map<String, Object> mapTemp = new HashMap<String, Object>();
                mapTemp.put("name", name);
                mapTemp.put("data", dataList);
                ydata.add(mapTemp);
            }

            AbTestResult abTestResult = new AbTestResult();
            abTestResult.setYData(ydata);
            abTestResult.setXList(sortList);
            return abTestResult;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * PV 明细公共方法
     *
     * @param abTestVo 查询参数
     * @return tableData：产品等级；tableData2：产品类目
     */
    private AbTestResult pvDetail(AbTestVo abTestVo) {
        return detail(abTestVo, "AbTestPv");
    }

    /**
     * UV 明细公共方法
     *
     * @param abTestVo 查询参数
     * @return tableData：产品等级；tableData2：产品类目
     */
    private AbTestResult uvDetail(AbTestVo abTestVo) {
        return detail(abTestVo, "AbTestUv");
    }

    /**
     * 明细公共实现：同时返回产品等级 + 产品类目
     */
    private AbTestResult detail(AbTestVo abTestVo, String tableName) {
        String timeinfo = abTestVo.getTimedate();
        int testFlag = Integer.parseInt(abTestVo.getVersiontype());

        List<Map<String, Object>> productLeaveList = queryProductGrade(tableName, timeinfo, testFlag);
        List<Map<String, Object>> producttypeList = queryProductType(tableName, timeinfo, testFlag);

        AbTestResult abTestResult = new AbTestResult();
        abTestResult.setTableData(productLeaveList);
        abTestResult.setTableData2(producttypeList);
        return abTestResult;
    }

    /**
     * 只返回产品等级分布
     */
    private AbTestResult buildGradeResult(AbTestVo abTestVo, String tableName) {
        String timeinfo = abTestVo.getTimedate();
        int testFlag = Integer.parseInt(abTestVo.getVersiontype());

        AbTestResult abTestResult = new AbTestResult();
        abTestResult.setTableData(queryProductGrade(tableName, timeinfo, testFlag));
        return abTestResult;
    }

    /**
     * 只返回产品类目分布
     */
    private AbTestResult buildTypeResult(AbTestVo abTestVo, String tableName) {
        String timeinfo = abTestVo.getTimedate();
        int testFlag = Integer.parseInt(abTestVo.getVersiontype());

        AbTestResult abTestResult = new AbTestResult();
        abTestResult.setTableData2(queryProductType(tableName, timeinfo, testFlag));
        return abTestResult;
    }

    /**
     * 查询产品等级分布
     *
     * @param tableName Doris 表名
     * @param timeinfo  时间字符串
     * @param testFlag  版本标识
     * @return 等级分布列表
     */
    private List<Map<String, Object>> queryProductGrade(String tableName, String timeinfo, int testFlag) {
        List<Map<String, Object>> resultList = new ArrayList<>();
        try {
            String sql = "select productypeleeave,numbers from " + tableName + " where timeinfo ='" + timeinfo + "' and testFlag = " + testFlag + ";";
            ResultSet resultSet = DorisDBUtil.quereBySql("jdbc:mysql://192.168.88.101:9030/dorisTest", "com.mysql.cj.jdbc.Driver", "root", "123456", sql);
            while (resultSet.next()) {
                int productypeleeave = resultSet.getInt("productypeleeave");
                long numbers = resultSet.getLong("numbers");
                Map<String, Object> map = new HashMap<String, Object>();
                map.put("date", timeinfo);
                map.put("name", productLeaveMap.get(productypeleeave));
                map.put("numbers", numbers);
                resultList.add(map);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resultList;
    }

    /**
     * 查询产品类目分布
     *
     * @param tableName Doris 表名
     * @param timeinfo  时间字符串
     * @param testFlag  版本标识
     * @return 类目分布列表
     */
    private List<Map<String, Object>> queryProductType(String tableName, String timeinfo, int testFlag) {
        List<Map<String, Object>> resultList = new ArrayList<>();
        try {
            String sql = "select productTypeid,numbers from " + tableName + " where timeinfo ='" + timeinfo + "' and testFlag = " + testFlag + ";";
            ResultSet resultSet = DorisDBUtil.quereBySql("jdbc:mysql://192.168.88.101:9030/dorisTest", "com.mysql.cj.jdbc.Driver", "root", "123456", sql);
            while (resultSet.next()) {
                int productTypeid = resultSet.getInt("productTypeid");
                String producttypename = HbaseUtil2.getdata("productType", productTypeid + "", "info", "producttypename");
                long numbers = resultSet.getLong("numbers");
                Map<String, Object> map = new HashMap<String, Object>();
                map.put("date", timeinfo);
                map.put("name", producttypename);
                map.put("numbers", numbers);
                resultList.add(map);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resultList;
    }

    /**
     * 兼容旧名的 PV 小时明细（原方法名拼写错误 pvHourDetial）
     */
    public AbTestResult pvHourDetial(AbTestVo abTestVo) {
        return pvHourDetail(abTestVo);
    }
}
