import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 测试数据生成器
 * 直接连接 Doris 数据库，插入模拟数据。
 *
 * 用法：java TestDataGenerator
 *
 * @author Rp
 * @date 2026.6.27
 */
public class TestDataGenerator {

    // Doris 配置
    private static final String DORIS_URL = "jdbc:mysql://192.168.88.101:9030/hadoop_db";
    private static final String DORIS_DRIVER = "com.mysql.cj.jdbc.Driver";
    private static final String DORIS_USER = "hadoop";
    private static final String DORIS_PWD = "hadoop";

    // 数据池
    private static final Random RAND = new Random();
    private static final SimpleDateFormat DAY_FORMAT = new SimpleDateFormat("yyyyMMdd");
    private static final SimpleDateFormat HOUR_FORMAT = new SimpleDateFormat("yyyyMMddHH");

    // 产品类目 IDs
    private static final long[] PRODUCT_TYPE_IDS = {101L, 102L, 103L, 104L, 105L};
    // ABTest 版本
    private static final int[] TEST_FLAGS = {1, 2, 3}; // 1=A版 2=B版 3=基本版
    // 设备类型
    private static final int[] DEVICE_TYPES = {0, 1, 2}; // 0=其他 1=移动端 2=PC端
    // 团购/秒杀/活动 IDs
    private static final long[] TUANGOU_IDS = {1L, 2L, 3L};
    private static final long[] MIAOSHA_IDS = {1L, 2L, 3L};
    private static final long[] HUODONG_IDS = {1L, 2L};

    private static Connection conn;

    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("  Doris 测试数据生成器启动");
        System.out.println("========================================");

        try {
            Class.forName(DORIS_DRIVER);
            conn = DriverManager.getConnection(DORIS_URL, DORIS_USER, DORIS_PWD);
            conn.setAutoCommit(false);
            System.out.println("Doris 连接成功\n");

            // 生成最近 10 天 + 最近 10 小时的时间字符串
            List<String> dayTimes = generateDayTimes(10);
            List<String> hourTimes = generateHourTimes(10);

            int total = 0;

            // 1. 订单数据 OrderTide
            System.out.println("[1/8] 插入订单数据 OrderTide...");
            total += insertOrderTide(dayTimes);

            // 2. 团购流量 MarketingLiuliangtuangou
            System.out.println("[2/8] 插入团购流量数据...");
            total += insertMarketingLiuliang("MarketingLiuliangtuangou", TUANGOU_IDS, dayTimes, hourTimes);

            // 3. 秒杀流量 MarketingLiuliangmiaosha
            System.out.println("[3/8] 插入秒杀流量数据...");
            total += insertMarketingLiuliang("MarketingLiuliangmiaosha", MIAOSHA_IDS, dayTimes, hourTimes);

            // 4. 大促流量 MarketingLiulianghuodong
            System.out.println("[4/8] 插入大促流量数据...");
            total += insertMarketingLiuliang("MarketingLiulianghuodong", HUODONG_IDS, dayTimes, hourTimes);

            // 5. 团购交易 MarketingDealtuangou
            System.out.println("[5/8] 插入团购交易数据...");
            total += insertMarketingDeal("MarketingDealtuangou", TUANGOU_IDS, dayTimes);

            // 6. 秒杀交易 MarketingDealmiaosha
            System.out.println("[6/8] 插入秒杀交易数据...");
            total += insertMarketingDeal("MarketingDealmiaosha", MIAOSHA_IDS, dayTimes);

            // 7. 大促交易 MarketingDealhuodong
            System.out.println("[7/8] 插入大促交易数据...");
            total += insertMarketingDeal("MarketingDealhuodong", HUODONG_IDS, dayTimes);

            // 8. 终端流量 TerminalLiuliang + 新增用户 TerminalNewUser
            System.out.println("[8/8] 插入终端流量和用户数据...");
            total += insertTerminalLiuliang(dayTimes, hourTimes);
            total += insertTerminalNewUser(dayTimes, hourTimes);

            // ABTest 数据
            System.out.println("[+1] 插入 ABTest PV/UV 数据...");
            total += insertAbTest("AbTestPv", dayTimes, hourTimes);
            total += insertAbTest("AbTestUv", dayTimes, hourTimes);
            total += insertAbTestTotal("AbTestPvTotal", dayTimes);
            total += insertAbTestTotal("AbTestUvTotal", dayTimes);

            conn.commit();

            System.out.println("\n========================================");
            System.out.println("  数据生成完毕！共插入 " + total + " 条记录");
            System.out.println("========================================");
            System.out.println("现在可以启动前端，用自然语言查询数据了");

        } catch (Exception e) {
            System.err.println("错误: " + e.getMessage());
            e.printStackTrace();
            try { if (conn != null) conn.rollback(); } catch (Exception ex) {}
        } finally {
            try { if (conn != null) conn.close(); } catch (Exception e) {}
        }
    }

    // ==================== 各表插入方法 ====================

    private static int insertOrderTide(List<String> dayTimes) throws SQLException {
        int count = 0;
        String sql = "INSERT INTO OrderTide (timeinfo, productypeid, numbers) VALUES (?, ?, ?)";
        PreparedStatement ps = conn.prepareStatement(sql);
        for (String t : dayTimes) {
            for (long productTypeId : PRODUCT_TYPE_IDS) {
                long numbers = 10 + RAND.nextInt(50);
                ps.setString(1, t);
                ps.setLong(2, productTypeId);
                ps.setLong(3, numbers);
                ps.addBatch();
                count++;
            }
        }
        ps.executeBatch();
        ps.close();
        System.out.println("      OrderTide: " + count + " 条");
        return count;
    }

    private static int insertMarketingLiuliang(String tableName, long[] ids, List<String> dayTimes, List<String> hourTimes) throws SQLException {
        int count = 0;
        String sql = "INSERT INTO " + tableName + " (timeinfo, " + getIdField(tableName) + ", numbers) VALUES (?, ?, ?)";
        PreparedStatement ps = conn.prepareStatement(sql);
        for (String t : dayTimes) {
            for (long id : ids) {
                long numbers = 20 + RAND.nextInt(100);
                ps.setString(1, t);
                ps.setLong(2, id);
                ps.setLong(3, numbers);
                ps.addBatch();
                count++;
            }
        }
        for (String t : hourTimes) {
            for (long id : ids) {
                long numbers = 5 + RAND.nextInt(30);
                ps.setString(1, t);
                ps.setLong(2, id);
                ps.setLong(3, numbers);
                ps.addBatch();
                count++;
            }
        }
        ps.executeBatch();
        ps.close();
        System.out.println("      " + tableName + ": " + count + " 条");
        return count;
    }

    private static int insertMarketingDeal(String tableName, long[] ids, List<String> dayTimes) throws SQLException {
        int count = 0;
        String sql = "INSERT INTO " + tableName + " (timeinfo, " + getIdField(tableName) + ", numbers) VALUES (?, ?, ?)";
        PreparedStatement ps = conn.prepareStatement(sql);
        for (String t : dayTimes) {
            for (long id : ids) {
                long numbers = 5 + RAND.nextInt(30);
                ps.setString(1, t);
                ps.setLong(2, id);
                ps.setLong(3, numbers);
                ps.addBatch();
                count++;
            }
        }
        ps.executeBatch();
        ps.close();
        System.out.println("      " + tableName + ": " + count + " 条");
        return count;
    }

    private static int insertTerminalLiuliang(List<String> dayTimes, List<String> hourTimes) throws SQLException {
        int count = 0;
        String sql = "INSERT INTO TerminalLiuliang (timeinfo, deviceType, numbers) VALUES (?, ?, ?)";
        PreparedStatement ps = conn.prepareStatement(sql);
        for (String t : dayTimes) {
            for (int deviceType : DEVICE_TYPES) {
                long numbers = 50 + RAND.nextInt(200);
                ps.setString(1, t);
                ps.setInt(2, deviceType);
                ps.setLong(3, numbers);
                ps.addBatch();
                count++;
            }
        }
        for (String t : hourTimes) {
            for (int deviceType : DEVICE_TYPES) {
                long numbers = 10 + RAND.nextInt(50);
                ps.setString(1, t);
                ps.setInt(2, deviceType);
                ps.setLong(3, numbers);
                ps.addBatch();
                count++;
            }
        }
        ps.executeBatch();
        ps.close();
        System.out.println("      TerminalLiuliang: " + count + " 条");
        return count;
    }

    private static int insertTerminalNewUser(List<String> dayTimes, List<String> hourTimes) throws SQLException {
        int count = 0;
        String sql = "INSERT INTO TerminalNewUser (timeinfo, deviceType, numbers) VALUES (?, ?, ?)";
        PreparedStatement ps = conn.prepareStatement(sql);
        for (String t : dayTimes) {
            for (int deviceType : DEVICE_TYPES) {
                long numbers = 10 + RAND.nextInt(50);
                ps.setString(1, t);
                ps.setInt(2, deviceType);
                ps.setLong(3, numbers);
                ps.addBatch();
                count++;
            }
        }
        for (String t : hourTimes) {
            for (int deviceType : DEVICE_TYPES) {
                long numbers = 2 + RAND.nextInt(15);
                ps.setString(1, t);
                ps.setInt(2, deviceType);
                ps.setLong(3, numbers);
                ps.addBatch();
                count++;
            }
        }
        ps.executeBatch();
        ps.close();
        System.out.println("      TerminalNewUser: " + count + " 条");
        return count;
    }

    private static int insertAbTest(String tableName, List<String> dayTimes, List<String> hourTimes) throws SQLException {
        int count = 0;
        String sql = "INSERT INTO " + tableName + " (timeinfo, testFlag, productypeleeave, productypeid, numbers) VALUES (?, ?, ?, ?, ?)";
        PreparedStatement ps = conn.prepareStatement(sql);
        for (String t : dayTimes) {
            for (int testFlag : TEST_FLAGS) {
                for (long productTypeId : PRODUCT_TYPE_IDS) {
                    long numbers = 20 + RAND.nextInt(100);
                    int productypeleeave = 1 + RAND.nextInt(3);
                    ps.setString(1, t);
                    ps.setInt(2, testFlag);
                    ps.setInt(3, productypeleeave);
                    ps.setLong(4, productTypeId);
                    ps.setLong(5, numbers);
                    ps.addBatch();
                    count++;
                }
            }
        }
        for (String t : hourTimes) {
            for (int testFlag : TEST_FLAGS) {
                long numbers = 5 + RAND.nextInt(30);
                int productypeleeave = 1 + RAND.nextInt(3);
                ps.setString(1, t);
                ps.setInt(2, testFlag);
                ps.setInt(3, productypeleeave);
                ps.setLong(4, PRODUCT_TYPE_IDS[RAND.nextInt(PRODUCT_TYPE_IDS.length)]);
                ps.setLong(5, numbers);
                ps.addBatch();
                count++;
            }
        }
        ps.executeBatch();
        ps.close();
        System.out.println("      " + tableName + ": " + count + " 条");
        return count;
    }

    private static int insertAbTestTotal(String tableName, List<String> dayTimes) throws SQLException {
        int count = 0;
        String sql = "INSERT INTO " + tableName + " (timeinfo, testFlag, numbers) VALUES (?, ?, ?)";
        PreparedStatement ps = conn.prepareStatement(sql);
        for (String t : dayTimes) {
            for (int testFlag : TEST_FLAGS) {
                long numbers = 200 + RAND.nextInt(500);
                ps.setString(1, t);
                ps.setInt(2, testFlag);
                ps.setLong(3, numbers);
                ps.addBatch();
                count++;
            }
        }
        ps.executeBatch();
        ps.close();
        System.out.println("      " + tableName + ": " + count + " 条");
        return count;
    }

    // ==================== 工具方法 ====================

    private static List<String> generateDayTimes(int days) {
        List<String> times = new ArrayList<>();
        Calendar cal = Calendar.getInstance();
        for (int i = days - 1; i >= 0; i--) {
            cal.setTime(new java.util.Date());
            cal.add(Calendar.DAY_OF_YEAR, -i);
            times.add(DAY_FORMAT.format(cal.getTime()));
        }
        return times;
    }

    private static List<String> generateHourTimes(int hours) {
        List<String> times = new ArrayList<>();
        Calendar cal = Calendar.getInstance();
        for (int i = hours - 1; i >= 0; i--) {
            cal.setTime(new java.util.Date());
            cal.add(Calendar.HOUR_OF_DAY, -i);
            times.add(HOUR_FORMAT.format(cal.getTime()));
        }
        return times;
    }

    private static String getIdField(String tableName) {
        if (tableName.contains("tuangou")) return "tuangouid";
        if (tableName.contains("miaosha")) return "miaoshaid";
        if (tableName.contains("huodong")) return "huodongid";
        return "id";
    }
}
