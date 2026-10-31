package cn.rp.util;

import com.alibaba.fastjson.JSONObject;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * 数据转换工具类
 * <p>
 * 作用：把 MySQL binlog / Kafka 消息里的 INSERT 数据，
 * 通过表名找到对应的 Java 实体类，用反射拿到字段名，
 * 最后把字段和值转成 Map，写入 HBase。
 * </p>
 *
 * @author Rp
 * @date 2026.6.17 16:32
 **/
public class TransferUtil {

    /**
     * 表名与实体类全路径的映射
     * <p>
     * key：Kafka 消息里传来的表名（小写开头，和 MySQL 表名一致）
     * value：对应实体类的全限定类名，用于反射获取字段
     * </p>
     */
    public static Map<String, String> tableEnityMap = new HashMap<String, String>();

    static {
        // productInfo 表对应 cn.rp.business.productInfo 类
        tableEnityMap.put("productInfo", "cn.rp.business.productInfo");
        // productType 表对应 cn.rp.business.productTypeInfo 类
        tableEnityMap.put("productType", "cn.rp.business.productTypeInfo");
        // Order 表对应 cn.rp.business.Order 类
        tableEnityMap.put("order", "cn.rp.business.Order");
        // miaoasha 表对应 cn.rp.business.MiaoashaInfo 类
        tableEnityMap.put("miaoasha", "cn.rp.business.MiaoashaInfo");
        // tuangou 表对应 cn.rp.business.TuangouIndo 类
        tableEnityMap.put("tuangou", "cn.rp.business.TuangouIndo");
        // huodong 表对应 cn.rp.business.HuodongInfo 类
        tableEnityMap.put("huodong", "cn.rp.business.HuodongInfo");
    }

    /**
     * 将 Kafka 消息中的单条 INSERT 数据转换并写入 HBase
     *
     * @param tableName Kafka 消息里的表名，例如 "productInfo"
     * @param jsonData  该表对应的单行数据，例如 {"id":"1","productTypeid":"100","originalPrice":"99.9","shopid":"10"}
     */
    public static void transferAndInsert(String tableName, JSONObject jsonData) {
        try {
        // 1. 根据表名找到对应的实体类全路径
        String className = tableEnityMap.get(tableName);
        if (className == null) {
            System.out.println("未找到表名对应的实体类：" + tableName);
            return;
        }


            // 2. 通过反射加载实体类
            Class<?> classez = Class.forName(className);

            // 3. 获取实体类中声明的所有字段（private 也能拿到）
            Field[] fields = classez.getDeclaredFields();

            // 4. 从 JSON 里取 id，作为 HBase 的 rowkey
            String id = jsonData.getString("id");
            if (id == null || id.trim().isEmpty()) {
                System.out.println("数据缺少 id 字段，无法作为 HBase rowkey：" + jsonData);
                return;
            }

            // 5. 如果 HBase 表不存在，先创建（列族为 "info"）
            HbaseUtil2.createTable(tableName, "info");

            // 6. 遍历实体类字段，把 JSON 中存在的字段放入 dataMap
            Map<String, String> datamap = new HashMap<String, String>();
            for (Field field : fields) {
                String fieldName = field.getName();
                // 从 JSON 取同名字段的值
                String valueString = jsonData.getString(fieldName);

                // 值不为空才放入 HBase，避免存一堆 null
                if (valueString != null && !valueString.trim().isEmpty()) {
                    datamap.put(fieldName, valueString);
                }
            }

            // 7. 调用 HBase 工具类写入数据
            // 参数：表名、rowkey、列族名、字段-值映射
            HbaseUtil2.put(tableName, id, "info", datamap);

        } catch (Exception e) {
            System.out.println("写入 HBase 失败，表名：" + tableName + "，数据：" + jsonData);
            e.printStackTrace();
        }
    }

    /**
     * 测试入口：直接调用 transferAndInsert，验证能否把数据写入 HBase
     * <p><b>注意：</b> 这段 main 方法只是用来本地测试的，
     * 实际生产环境由 TransferAnaly 调用 transferAndInsert。</p>
     */


}
