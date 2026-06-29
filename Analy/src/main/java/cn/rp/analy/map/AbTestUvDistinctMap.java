package cn.rp.analy.abtest.map;

import cn.rp.entity.AbTestPvResult;
import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.util.Collector;

/**
 * @author Rp
 * @date 2026.6.9 17:46
 **/
public class AbTestUvDistinctMap implements FlatMapFunction<AbTestPvResult, AbTestPvResult> {
    @Override
    public void flatMap(AbTestPvResult abTestPvResult, Collector<AbTestPvResult> collector) throws Exception {
        String timeinfo = abTestPvResult.getTimeinfo();
        long productypeleeave = abTestPvResult.getProductypeleeave();
        long productypeid = abTestPvResult.getProductypeid();
        String groupField = "abtestuvDistinc==" +"=="+ timeinfo +"=="+ productypeleeave +"=="+ productypeid;



        AbTestPvResult abTestPvResultDay = new AbTestPvResult();
        abTestPvResultDay.setGroupField(groupField);
        abTestPvResultDay.setNumbers(1l);
        abTestPvResultDay.setProductypeleeave(productypeleeave);
        abTestPvResultDay.setProductypeid(productypeid);
        abTestPvResultDay.setTimeinfo(timeinfo);

        collector.collect(abTestPvResultDay);

    }
}