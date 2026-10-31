package cn.rp.analy.orderAnaly.map;

import cn.rp.business.Order;
import cn.rp.entity.OrderAnalyInfoResult;
import cn.rp.entity.TerminalInfoResult;
import cn.rp.log.ScanLog;
import cn.rp.util.DateUtil;
import cn.rp.util.HbaseUtil2;
import com.alibaba.fastjson.JSONObject;
import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.util.Collector;

import java.util.Date;


/**
 * @author Rp
 * @date 2026.6.9 17:46
 **/
public class OrderAnalyMap implements FlatMapFunction<String, OrderAnalyInfoResult> {

    @Override
    public void flatMap(String value, Collector<OrderAnalyInfoResult> collector) throws Exception {
        Order order = JSONObject.parseObject(value, Order.class);

        Date time = order.getCreateTime();
        if (time == null) {
            return;
        }

        String daytime = DateUtil.getDay(time);
        String hourtime = DateUtil.getHour(time);

        long numbers = 1L;
        String groupFieldDay = "orderAmount ==" + daytime;
        String groupFieldHour = "orderAmount ==" + hourtime;

        OrderAnalyInfoResult orderAnalyInfoResult = new OrderAnalyInfoResult();

        orderAnalyInfoResult.setProductypeid(order.getProductid());
        orderAnalyInfoResult.setTimeInfo(daytime);
        orderAnalyInfoResult.setNumbers(numbers);
        orderAnalyInfoResult.setGroupField(groupFieldDay);
        collector.collect(orderAnalyInfoResult);
        orderAnalyInfoResult = new OrderAnalyInfoResult();
        orderAnalyInfoResult.setProductypeid(order.getProductid());
        orderAnalyInfoResult.setTimeInfo(hourtime);
        orderAnalyInfoResult.setNumbers(numbers);
        orderAnalyInfoResult.setGroupField(groupFieldHour);
        collector.collect(orderAnalyInfoResult);

        /**
         * 类目趋势
         *
         */

        long productid = order.getProductid();
        String productTypeidStr = HbaseUtil2.getdata("productinfo", productid + "", "info", "producttypeid");
        long productTypeid = 0L;
        if (productTypeidStr != null && !productTypeidStr.isEmpty()) {
            productTypeid = Long.parseLong(productTypeidStr);
        }
        groupFieldDay = "orderproductType ==" + daytime + productTypeid;
        groupFieldHour = "orderproductType ==" + hourtime + productTypeid;
        orderAnalyInfoResult = new OrderAnalyInfoResult();

        orderAnalyInfoResult.setProductypeid(productTypeid);
        orderAnalyInfoResult.setTimeInfo(daytime);
        orderAnalyInfoResult.setNumbers(numbers);
        orderAnalyInfoResult.setGroupField(groupFieldDay);
        collector.collect(orderAnalyInfoResult);

        orderAnalyInfoResult = new OrderAnalyInfoResult();
        orderAnalyInfoResult.setProductypeid(productTypeid);
        orderAnalyInfoResult.setTimeInfo(hourtime);
        orderAnalyInfoResult.setNumbers(numbers);
        orderAnalyInfoResult.setGroupField(groupFieldHour);
        collector.collect(orderAnalyInfoResult);

    }

}