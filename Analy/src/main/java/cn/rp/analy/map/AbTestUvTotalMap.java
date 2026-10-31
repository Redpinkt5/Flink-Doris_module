package cn.rp.analy.map;

import cn.rp.entity.AbTestPvResult;
import org.apache.flink.api.common.functions.MapFunction;

/**
 * @author Rp
 * @date 2026.6.10 08:02
 **/
public class AbTestUvTotalMap implements MapFunction<AbTestPvResult, AbTestPvResult> {
    @Override
    public AbTestPvResult map(AbTestPvResult abTestPvResult) throws Exception {

        String timeString = abTestPvResult.getTimeinfo();
        int testFlag = abTestPvResult.getTestFlag();

        String groupFieldDay = "abtestuvTotal==" + timeString + testFlag;

        long numbers = 11;

        AbTestPvResult abTestPvResultDay = new AbTestPvResult();
        abTestPvResultDay.setGroupField(groupFieldDay);
        abTestPvResultDay.setNumbers(numbers);
        abTestPvResultDay.setTimeinfo(timeString);

        return abTestPvResultDay;
    }


}