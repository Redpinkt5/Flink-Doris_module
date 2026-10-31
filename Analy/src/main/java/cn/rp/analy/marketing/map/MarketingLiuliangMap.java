package cn.rp.analy.marketing.map;

import cn.rp.entity.AbTestPvResult;
import cn.rp.entity.MarketingInfoResult;
import cn.rp.log.ScanLog;
import cn.rp.util.DateUtil;
import com.alibaba.fastjson.JSONObject;
import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.util.Collector;

/**
 * @author Rp
 * @date 2026.6.18 16:33
 **/
public class MarketingLiuliangMap implements FlatMapFunction<String, MarketingInfoResult> {
    @Override
    public void flatMap(String s, Collector<MarketingInfoResult> collector) throws Exception {
        ScanLog scanLog = JSONObject.parseObject(s, ScanLog.class);
        long time = scanLog.getTime();

        String daytime = DateUtil.getDay(time);
        String hourtime = DateUtil.getHour(time);
        long numbers = 1L;

        long tuangouid = scanLog.getTuangouid();
        long miaoshaid = scanLog.getMiaoshaid();
        long huodongid = scanLog.getHuodongid();

        if(tuangouid> 0){
            String groupFieldDay = "Marketingtuangou =="+daytime + tuangouid;
            String groupFieldHour = "Marketingtuangou =="+hourtime + tuangouid;

            MarketingInfoResult marketingInfoResult = new MarketingInfoResult();

            marketingInfoResult.setGroupFieldId(groupFieldDay);
            marketingInfoResult.setTuangouid(tuangouid);
            marketingInfoResult.setNumbers(numbers);
            marketingInfoResult.setTimeinfo(daytime);
            collector.collect(marketingInfoResult);

            marketingInfoResult.setGroupFieldId(groupFieldHour);
            marketingInfoResult.setTuangouid(tuangouid);
            marketingInfoResult.setNumbers(numbers);
            marketingInfoResult.setTimeinfo(hourtime);
            collector.collect(marketingInfoResult);

        }
        if(miaoshaid> 0){

            String groupFieldDay = "Marketingmiaosha =="+daytime + miaoshaid;
            String groupFieldHour = "Marketingmiaosha =="+hourtime + miaoshaid;

            MarketingInfoResult marketingInfoResult = new MarketingInfoResult();

            marketingInfoResult.setGroupFieldId(groupFieldDay);
            marketingInfoResult.setMiaoshaid(miaoshaid);
            marketingInfoResult.setNumbers(numbers);
            marketingInfoResult.setTimeinfo(daytime);
            collector.collect(marketingInfoResult);

            marketingInfoResult.setGroupFieldId(groupFieldHour);
            marketingInfoResult.setMiaoshaid(miaoshaid);
            marketingInfoResult.setNumbers(numbers);
            marketingInfoResult.setTimeinfo(hourtime);
            collector.collect(marketingInfoResult);

        }
        if(huodongid> 0){

            String groupFieldDay = "Marketinghuodong =="+daytime + huodongid;
            String groupFieldHour = "Marketinghuodong =="+hourtime + huodongid;

            MarketingInfoResult marketingInfoResult = new MarketingInfoResult();

            marketingInfoResult.setGroupFieldId(groupFieldDay);
            marketingInfoResult.setHuodongid(huodongid);
            marketingInfoResult.setNumbers(numbers);
            marketingInfoResult.setTimeinfo(daytime);
            collector.collect(marketingInfoResult);

            marketingInfoResult.setGroupFieldId(groupFieldHour);
            marketingInfoResult.setHuodongid(huodongid);
            marketingInfoResult.setNumbers(numbers);
            marketingInfoResult.setTimeinfo(hourtime);
            collector.collect(marketingInfoResult);

        }
    }
}