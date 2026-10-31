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
 * @date 2026.6.10 08:41
 **/
public class AbTestTotalLeavePvSink implements SinkFunction<AbTestPvResult> {
    @Override
    public void invoke(AbTestPvResult value, Context context) throws Exception {
        Map<String, Object> dataMap = new HashMap<String, Object>();

        int testFlag = value.getTestFlag();
        String timeinfo = value.getTimeinfo();

        long numbers = value.getNumbers();

        dataMap.put("timeinfo", timeinfo);
        dataMap.put("numbers", numbers);
        dataMap.put("testFlag",testFlag);
        Set<String> intFieldSet = new HashSet<>();
        intFieldSet.add("numbers");
        DorisDBUtil.saveData("AbTestPvTotal", dataMap, intFieldSet, "com.mysql.cj.jdbc.Driver", "hadoop", "hadoop");
    }
}