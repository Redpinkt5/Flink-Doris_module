package cn.rp.analy.abtest.reduce;

import cn.rp.entity.AbTestPvResult;
import org.apache.flink.api.common.functions.ReduceFunction;

/**
 * @author Rp
 * @date 2026.6.10 05:19
 **/
public class AbTestReduce implements ReduceFunction<AbTestPvResult>{
    @Override
    public AbTestPvResult reduce(AbTestPvResult abTestPvResult, AbTestPvResult t1) throws Exception {
        long numbers1 = abTestPvResult.getNumbers();
        long numbers2 = t1.getNumbers();

        abTestPvResult.setNumbers(numbers1+numbers2);
        return null;
    }
}