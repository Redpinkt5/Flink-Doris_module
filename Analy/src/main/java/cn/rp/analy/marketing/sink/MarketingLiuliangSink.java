package cn.rp.analy.marketing.sink;

import cn.rp.entity.MarketingInfoResult;
import cn.rp.util.DorisDBUtil;
import org.apache.flink.streaming.api.functions.sink.SinkFunction;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author Rp
 * @date 2026.6.10 05:47
 **/
public class MarketingLiuliangSink implements SinkFunction<MarketingInfoResult> {
    @Override
    public void invoke(MarketingInfoResult value, Context context) throws Exception {

        String timeinfo = value.getTimeinfo();
        long numbers = value.getNumbers();
        long huodongid = value.getHuodongid();
        long tuangouid = value.getTuangouid();
        long miaoshaid = value.getMiaoshaid();

        Set<String> intFieldSet = new HashSet<>();
        intFieldSet.add("huodongid");
        intFieldSet.add("tuangouid");
        intFieldSet.add("miaoshaid");
        intFieldSet.add("numbers");

        if(huodongid> 0){
            Map<String, Object> dataMap = new HashMap<String, Object>();
            dataMap.put("huodongid", huodongid);
            dataMap.put("timeinfo", timeinfo);
            dataMap.put("numbers", numbers);
            DorisDBUtil.saveData("MarketingLiulianghuodong", dataMap, intFieldSet, "com.mysql.cj.jdbc.Driver", "hadoop", "hadoop");

        }
        if(tuangouid> 0){
            Map<String, Object> dataMap = new HashMap<String, Object>();
            dataMap.put("tuangouid", tuangouid);
            dataMap.put("timeinfo", timeinfo);
            dataMap.put("numbers", numbers);
            DorisDBUtil.saveData("MarketingLiuliangtuangou", dataMap, intFieldSet, "com.mysql.cj.jdbc.Driver", "hadoop", "hadoop");

        }
        if(miaoshaid> 0){
            Map<String, Object> dataMap = new HashMap<String, Object>();
            dataMap.put("miaoshaid", miaoshaid);
            dataMap.put("timeinfo", timeinfo);
            dataMap.put("numbers", numbers);
            DorisDBUtil.saveData("MarketingLiuliangmiaosha", dataMap, intFieldSet, "com.mysql.cj.jdbc.Driver", "hadoop", "hadoop");

        }
    }
}