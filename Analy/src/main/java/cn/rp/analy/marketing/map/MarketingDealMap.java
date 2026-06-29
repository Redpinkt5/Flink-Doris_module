package cn.rp.analy.abtest.marketing.map;

import cn.rp.business.Order;
import cn.rp.entity.MarketingInfoResult;
import cn.rp.util.DateUtil;
import com.alibaba.fastjson.JSONObject;
import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.util.Collector;

import java.util.Date;

/**
 * @author Rp
 * @date 2026.6.18 16:33
 **/
public class MarketingDealMap implements FlatMapFunction<String, MarketingInfoResult> {
    @Override
    public void flatMap(String s, Collector<MarketingInfoResult> collector) throws Exception {
        Order order = JSONObject.parseObject(s, Order.class);
        Date Time = order.getCreateTime();
        String daytime = DateUtil.getDay(Time);
        String hourtime = DateUtil.getHour(Time);

        long numbers = 1L;

        long tuangouid = order.getTuangouid();
        long miaoshaid = order.getMiaoshaid();
        long huodongid = order.getHuodongid();

        if(tuangouid> 0){
            String groupFieldDay = "MarketingtuangouDeal =="+daytime + tuangouid;
            String groupFieldHour = "MarketingtuangouDeal =="+hourtime + tuangouid;

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

            String groupFieldDay = "MarketingmiaoshaDeal =="+daytime + miaoshaid;
            String groupFieldHour = "MarketingmiaoshaDeal =="+hourtime + miaoshaid;

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

            String groupFieldDay = "MarketinghuodongDeal =="+daytime + huodongid;
            String groupFieldHour = "MarketinghuodongDeal =="+hourtime + huodongid;

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