package cn.rp.analy.orderAnaly.reduce;

import cn.rp.entity.OrderAnalyInfoResult;
import cn.rp.entity.TerminalInfoResult;
import org.apache.flink.api.common.functions.ReduceFunction;

/**
 * @author Rp
 * @date 2026.6.10 05:19
 **/
public class OrderAnalyReduce implements ReduceFunction<OrderAnalyInfoResult>{
    @Override
    public OrderAnalyInfoResult reduce(OrderAnalyInfoResult orderAnalyInfoResult, OrderAnalyInfoResult t1) throws Exception {
        long numbers1 = orderAnalyInfoResult.getNumbers();
        long numbers2 = t1.getNumbers();

        orderAnalyInfoResult.setNumbers(numbers1+numbers2);
        return orderAnalyInfoResult;
    }
}