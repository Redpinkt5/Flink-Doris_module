package cn.rp.analy.abtest.map;

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

        String timeString = abTestPvResult.getTimeinfo();
        String groupFieldDay = "abtestpvTotal==" + timeString;


        long numbers = 11;

        AbTestPvResult abTestPvResultDay = new AbTestPvResult();
        abTestPvResultDay.setGroupField(groupFieldDay);
        abTestPvResultDay.setNumbers(numbers);
        abTestPvResultDay.setTimeinfo(timeString);

        return abTestPvResultDay;
    }


}