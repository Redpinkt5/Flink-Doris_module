package cn.rp.analy.sink;

import cn.rp.entity.AbTestPvResult;
import cn.rp.util.DorisDBUtil;
import org.apache.flink.streaming.api.functions.sink.SinkFunction;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author Rp
 * @date 2026.6.10 05:47
 **/
public class AbTestLeavePvSink implements SinkFunction<AbTestPvResult> {
    @Override
    public void invoke(AbTestPvResult value) throws Exception {
        Map<String, Object> dataMap = new HashMap<String, Object>();

        String timeinfo = value.getTimeinfo();

        long numbers = value.getNumbers();
        long productypeid = value.getProductypeid();
        int testFlag = value.getTestFlag();

        long productypeleeave = value.getProductypeleeave();

        dataMap.put("timeinfo", timeinfo);
        dataMap.put("numbers", numbers);
        dataMap.put("testFlag", testFlag);
        dataMap.put("productypeleeave", productypeleeave);

        dataMap.put("productypeid", productypeid);
        Set<String> intFieldSet = new HashSet<>();
        intFieldSet.add("numbers");
        intFieldSet.add("productypeid");
        intFieldSet.add("productypeleeave");
        DorisDBUtil.saveData("AbTestPv", dataMap, intFieldSet, "com.mysql.cj.jdbc.Driver", "hadoop", "hadoop");

    }

}