package cn.rp.analy.abtest.map;

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
public class AbTestUvMap implements FlatMapFunction<String, AbTestPvResult> {

    @Override
    public void flatMap(String value, Collector<AbTestPvResult> collector) throws Exception {
        ScanLog scanLog = JSONObject.parseObject(value, ScanLog.class);
        long time = scanLog.getTime();

        String daytime = DateUtil.getDay(time);
        String hourtime = DateUtil.getHour(time);

        long numbers = 1;
        long productypeleeave = scanLog.getProductypeleeave();
        long productypeid = scanLog.getProductypeid();
        long userid = scanLog.getUserid();// 用户id
        if(userid < 0){
            return;
        }
        String groupFieldDay = "abtestuv==" +"=="+ daytime +"=="+ productypeleeave +"=="+ productypeid +"=="+ userid;
        String groupFieldHour = "abtestuv==" +"=="+ hourtime +"=="+ productypeleeave +"=="+ productypeid +"=="+ userid;

        AbTestPvResult abTestPvResultDay = new AbTestPvResult();
        abTestPvResultDay.setGroupField(groupFieldDay);
        abTestPvResultDay.setNumbers(numbers);
        abTestPvResultDay.setProductypeleeave(productypeleeave);
        abTestPvResultDay.setProductypeid(productypeid);
        abTestPvResultDay.setTimeinfo(daytime);
        abTestPvResultDay.setUserid(userid);

        collector.collect(abTestPvResultDay);

        AbTestPvResult abTestPvResultHour = new AbTestPvResult();
        abTestPvResultHour.setGroupField(groupFieldHour);
        abTestPvResultHour.setNumbers(numbers);
        abTestPvResultHour.setProductypeleeave(productypeleeave);
        abTestPvResultHour.setProductypeid(productypeid);
        abTestPvResultHour.setTimeinfo(hourtime);
        abTestPvResultHour.setUserid(userid);


        collector.collect(abTestPvResultHour);
    }

}