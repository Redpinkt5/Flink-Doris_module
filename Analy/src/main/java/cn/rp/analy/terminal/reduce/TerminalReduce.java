package cn.rp.analy.abtest.terminal.reduce;

import cn.rp.entity.AbTestPvResult;
import cn.rp.entity.TerminalInfoResult;
import org.apache.flink.api.common.functions.ReduceFunction;

/**
 * @author Rp
 * @date 2026.6.10 05:19
 **/
public class TerminalReduce implements ReduceFunction<TerminalInfoResult>{
    @Override
    public TerminalInfoResult reduce(TerminalInfoResult terminalInfoResult, TerminalInfoResult t1) throws Exception {
        long numbers1 = terminalInfoResult.getNumbers();
        long numbers2 = t1.getNumbers();

        terminalInfoResult.setNumbers(numbers1+numbers2);
        return terminalInfoResult;
    }
}