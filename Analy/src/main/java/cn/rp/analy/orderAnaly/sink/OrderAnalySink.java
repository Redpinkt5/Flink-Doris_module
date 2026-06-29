package cn.rp.analy.terminal.sink;

import cn.rp.entity.TerminalInfoResult;
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
public class TerminalLiuliangSink implements SinkFunction<TerminalInfoResult> {
    @Override
    public void invoke(TerminalInfoResult value, Context context) throws Exception {
        Map<String, Object> dataMap = new HashMap<String, Object>();

        String timeinfo = value.getTimeInfo();

        long numbers = value.getNumbers();
        int deviceType = value.getDeviceType();

        dataMap.put("timeinfo", timeinfo);
        dataMap.put("numbers", numbers);
        dataMap.put("deviceType", deviceType);

        Set<String> intFieldSet = new HashSet<>();
        intFieldSet.add("numbers");
        intFieldSet.add("deviceType");
        DorisDBUtil.saveData("TerminalLiuliang", dataMap, intFieldSet, "com.mysql.cj.jdbc.Driver", "root", "123456");
    }
}