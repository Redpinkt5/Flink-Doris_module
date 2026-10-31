package cn.rp.util;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.util.Bytes;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;




/**
 * @author Rp
 * @date 2026.6.14 17:48
 **/
public class HbaseUtil2 {
    private static Admin admin = null;
    private static Connection conn = null;
    static{
        //创建hbase配置对象
        Configuration conf = HBaseConfiguration.create();
        conf.set("hbase.rootdir","hdfs://node1:8020/hbase");

        conf.set("hbase.zookeeper.quorum","node1");
        conf.set("hbase.client.scanner.timeout.period","600000");
        conf.set("hbase.rpc.timeout","600000");
        conf.set("hbase.client.retries.number","3");
        conf.set("hbase.client.pause","1000");


        try {
            conn = ConnectionFactory.createConnection(conf);
            //得到管理程序
            admin = conn.getAdmin();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public static void createTable(String tabName,String familyName) throws IOException {
        HTableDescriptor tab = new HTableDescriptor(tabName);
        //添加列族，每个表至少有一个列族
        HColumnDescriptor colDesc = new HColumnDescriptor(familyName);
        tab.addFamily(colDesc);
        if (admin.tableExists(TableName.valueOf(tabName))) {
            System.out.println("表已存在：" + tabName);
            return;
        }
        //创建表
        HbaseUtil2.admin.createTable(tab);
        System.out.println("创建表成功：" + tabName);


    }
    public static void put(String tablename, String rowkey, String familyname, Map<String,String> datamap) throws IOException {
        Table table = conn.getTable(TableName.valueOf(tablename));
        //将字符串转化成byte[]
        byte[] rowkeybyte = Bytes.toBytes(rowkey);

        Put put = new Put(rowkeybyte);

        if(datamap != null){
            Set<Map.Entry<String, String>> set = datamap.entrySet();

            for(Map.Entry<String,String> entry : set){
                String key = entry.getKey();
                Object value = entry.getValue();
                put.addColumn(Bytes.toBytes(familyname),Bytes.toBytes(key),Bytes.toBytes(value.toString()));

            }
        }

        table.put(put);
        table.close();
        System.out.println("插入成功");

    }
    /**
     * 获取数据
     *
     */
    public static String getdata(String tablename, String rowkey, String familyname, String column) throws IOException {
        Table table = conn.getTable(TableName.valueOf(tablename));
        //将字符串转换成byte[]
        byte[] rowkeybyte = Bytes.toBytes(rowkey);
        Get get = new Get(rowkeybyte);
        Result result = table.get(get);
        byte[] resultbytes = result.getValue(Bytes.toBytes(familyname), Bytes.toBytes(column));
        if(resultbytes != null){
            return new String(resultbytes);
        }else{
            return null;
        }
    }

    /**
     * 插入数据
     */

    public static void putdata(String tablename,String rowkey,String familyname,String column,String data) throws IOException {
        Table table = conn.getTable(TableName.valueOf(tablename));
        Put put = new Put(rowkey.getBytes());
        put.addColumn(familyname.getBytes(),column.getBytes(),data.getBytes());
        table.put(put);
    }

    public static void main(String[] args) throws IOException {

//        System.setProperties("hadoop.home.dir",);
//        createTable("testinfo","time");
//        putdata("testinfo","1","time","name","张三");
//
//        putdata("testinfo","1","cf","age","18");
        Map<String, String> datamap = new HashMap<>();
        datamap.put("info1","wsx");
        datamap.put("info2","wd");
        datamap.put("info3","sx");
        put("testinfo","2","time",datamap);

        String result = HbaseUtil2.getdata("testinfo","2","time","info1");
        String getdata = HbaseUtil2.getdata("testinfo", "2", "time", "info2");
        String getdata1 = HbaseUtil2.getdata("testinfo", "2", "time", "info3");
        result = result + getdata + getdata1;
        System.out.println("查询结果"+result);
    }
}
