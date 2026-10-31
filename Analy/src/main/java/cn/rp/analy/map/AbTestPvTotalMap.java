package cn.rp.analy.map;

import cn.rp.entity.AbTestPvResult;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.util.Collector;

/**
 * @author Rp
 * @date 2026.6.10 08:02
 **/
public class AbTestPvTotalMap implements MapFunction<AbTestPvResult, AbTestPvResult> {
    @Override
    public AbTestPvResult map(AbTestPvResult abTestPvResult) throws Exception {

        int testFlag = abTestPvResult.getTestFlag();
        String timeString = abTestPvResult.getTimeinfo();
        String groupFieldDay = "abtestpvTotal==" + timeString +testFlag;


        long numbers = 11;

        AbTestPvResult abTestPvResultDay = new AbTestPvResult();
        abTestPvResultDay.setGroupField(groupFieldDay);
        abTestPvResultDay.setNumbers(numbers);
        abTestPvResultDay.setTestFlag(testFlag);
        abTestPvResultDay.setTimeinfo(timeString);

        return abTestPvResultDay;
    }


}