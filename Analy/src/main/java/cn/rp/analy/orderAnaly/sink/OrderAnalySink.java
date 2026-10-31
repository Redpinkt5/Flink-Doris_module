package cn.rp.analy.orderAnaly.sink;

import cn.rp.entity.OrderAnalyInfoResult;
import cn.rp.util.DorisDBUtil;

import org.apache.flink.streaming.api.functions.sink.SinkFunction;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author Rp
 * @date 2026.6.10 08:41
 **/
public class OrderAnalySink implements SinkFunction<OrderAnalyInfoResult> {
    @Override
    public void invoke(OrderAnalyInfoResult value, Context context) throws Exception {
        Map<String, Object> dataMap = new HashMap<String, Object>();

        String timeinfo = value.getTimeInfo();
        long numbers = value.getNumbers();
        long productypeid = value.getProductypeid();

        dataMap.put("timeinfo", timeinfo);
        dataMap.put("numbers", numbers);
        dataMap.put("productypeid", productypeid);

        Set<String> intFieldSet = new HashSet<>();
        intFieldSet.add("numbers");
        intFieldSet.add("productypeid");

        DorisDBUtil.saveData("OrderTide", dataMap, intFieldSet, "com.mysql.cj.jdbc.Driver", "hadoop", "hadoop");
    }
}