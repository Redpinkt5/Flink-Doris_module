package cn.rp.analy.marketing.reduce;

import cn.rp.entity.AbTestPvResult;
import cn.rp.entity.MarketingInfoResult;
import org.apache.flink.api.common.functions.ReduceFunction;

/**
 * @author Rp
 * @date 2026.6.10 05:19
 **/
public class MarketingReduce implements ReduceFunction<MarketingInfoResult>{

    @Override
    public MarketingInfoResult reduce(MarketingInfoResult marketingInfoResult, MarketingInfoResult t1) throws Exception {
        long numbers1 = marketingInfoResult.getNumbers();
        long numbers2 = t1.getNumbers();

        marketingInfoResult.setNumbers(numbers1+numbers2);
        return marketingInfoResult;
    }
}