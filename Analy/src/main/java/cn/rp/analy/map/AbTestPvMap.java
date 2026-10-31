package cn.rp.analy.map;

import cn.rp.entity.AbTestPvResult;
import cn.rp.log.ScanLog;
import cn.rp.util.DateUtil;
import com.alibaba.fastjson.JSONObject;
import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.util.Collector;

/**
 * @author Rp
 * @date 2026.6.9 17:46
 **/
public class AbTestPvMap implements FlatMapFunction<String, AbTestPvResult> {

    @Override
    public void flatMap(String value, Collector<AbTestPvResult> collector) throws Exception {
        ScanLog scanLog = JSONObject.parseObject(value, ScanLog.class);
        long time = scanLog.getTime();

        String daytime = DateUtil.getDay(time);
        String hourtime = DateUtil.getHour(time);

        long numbers = 1;
        int testFlag = scanLog.getTestFlag();//AB基本三个版本

        long productypeleeave = scanLog.getProductypeleeave();
        long productypeid = scanLog.getProductypeid();
        String groupFieldDay = "abtestpv==" + daytime + productypeleeave + productypeid + testFlag;
        String groupFieldHour = "abtestpv==" + hourtime + productypeleeave + productypeid + testFlag;


        AbTestPvResult abTestPvResultDay = new AbTestPvResult();
        abTestPvResultDay.setGroupField(groupFieldDay);
        abTestPvResultDay.setNumbers(numbers);
        abTestPvResultDay.setProductypeleeave(productypeleeave);
        abTestPvResultDay.setTestFlag(testFlag);
        abTestPvResultDay.setProductypeid(productypeid);
        abTestPvResultDay.setTimeinfo(daytime);

        collector.collect(abTestPvResultDay);

        AbTestPvResult abTestPvResultHour = new AbTestPvResult();
        abTestPvResultHour.setGroupField(groupFieldHour);
        abTestPvResultHour.setNumbers(numbers);
        abTestPvResultHour.setTestFlag(testFlag);
        abTestPvResultHour.setProductypeleeave(productypeleeave);
        abTestPvResultHour.setProductypeid(productypeid);
        abTestPvResultHour.setTimeinfo(hourtime);


        collector.collect(abTestPvResultHour);
    }

}