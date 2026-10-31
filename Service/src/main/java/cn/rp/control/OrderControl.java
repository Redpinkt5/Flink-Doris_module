package cn.rp.control;

import cn.rp.result.OrderAnalyResult;
import cn.rp.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * 订单分析数据接口
 *
 * @author Rp
 * @date 2026.6.23 18:14
 **/
@RestController
@CrossOrigin
public class OrderControl {

    @Autowired
    private OrderService orderService;

    @RequestMapping(value = "/orderDayTide", method = RequestMethod.POST)
    public OrderAnalyResult orderDayTide() {
        return orderService.orderDayTide();
    }

    @RequestMapping(value = "/orderHourTide", method = RequestMethod.POST)
    public OrderAnalyResult orderHourTide() {
        return orderService.orderHourTide();
    }

    @RequestMapping(value = "/orderCategoryDayTide", method = RequestMethod.POST)
    public OrderAnalyResult orderCategoryDayTide() {
        return orderService.orderCategoryDayTide();
    }

    @RequestMapping(value = "/orderCategoryHourTide", method = RequestMethod.POST)
    public OrderAnalyResult orderCategoryHourTide() {
        return orderService.orderCategoryHourTide();
    }

    // 兼容旧名
    @RequestMapping(value = "/orderTrend", method = RequestMethod.POST)
    public OrderAnalyResult orderTrend() {
        return orderService.orderDayTide();
    }

    @RequestMapping(value = "/orderCategoryTrend", method = RequestMethod.POST)
    public OrderAnalyResult orderCategoryTrend() {
        return orderService.orderCategoryDayTide();
    }
}
