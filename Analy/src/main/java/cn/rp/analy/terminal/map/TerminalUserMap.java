package cn.rp.analy.terminal.map;

import cn.rp.entity.TerminalInfoResult;
import cn.rp.log.ScanLog;
import cn.rp.util.DateUtil;
import com.alibaba.fastjson.JSONObject;
import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.util.Collector;

/**
 * @author Rp
 * @date 2026.6.9 17:46
 **/
public class TerminalUserMap implements FlatMapFunction<String, TerminalInfoResult> {

    @Override
    public void flatMap(String value, Collector<TerminalInfoResult> collector) throws Exception {
        ScanLog scanLog = JSONObject.parseObject(value, ScanLog.class);
        long time = scanLog.getTime();

        String daytime = DateUtil.getDay(time);
        String hourtime = DateUtil.getHour(time);

        boolean isNewUser = scanLog.isIsNewUser();
        if(isNewUser !=  true){
            return;
        }




        long numbers = 1;
        int deviceType = scanLog.getDeviceType();

        long productypeid = scanLog.getProductypeid();
        String groupFieldDay = "TerminalIser ==" + daytime + deviceType;
        String groupFieldHour = "TerminalIser ==" + hourtime + deviceType;

        TerminalInfoResult terminalInfoResultDay = new TerminalInfoResult();
        terminalInfoResultDay.setGroupField(groupFieldDay);
        terminalInfoResultDay.setDeviceType(deviceType);
        terminalInfoResultDay.setNumbers(numbers);
        terminalInfoResultDay.setTimeInfo(daytime);

        collector.collect(terminalInfoResultDay);

        TerminalInfoResult terminalInfoResultHour = new TerminalInfoResult();
        terminalInfoResultHour.setGroupField(groupFieldHour);
        terminalInfoResultHour.setDeviceType(deviceType);
        terminalInfoResultHour.setNumbers(numbers);
        terminalInfoResultHour.setTimeInfo(hourtime);

        collector.collect(terminalInfoResultHour);
    }

}