package cn.rp.control;

import cn.rp.result.MarketingAnalyResult;
import cn.rp.service.MarketingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * 营销分析数据接口
 *
 * @author Rp
 * @date 2026.6.23 18:14
 **/
@RestController
@CrossOrigin
public class MarketingControl {

    @Autowired
    private MarketingService marketingService;

    // ==================== 流量（浏览）趋势 ====================

    @RequestMapping(value = "/tuangouLiuliang", method = RequestMethod.POST)
    public MarketingAnalyResult tuangouLiuliang() {
        return marketingService.tuangouLiuliang();
    }

    @RequestMapping(value = "/tuangouLiuliangDayTide", method = RequestMethod.POST)
    public MarketingAnalyResult tuangouLiuliangDayTide() {
        return marketingService.tuangouLiuliangDayTide();
    }

    @RequestMapping(value = "/tuangouLiuliangHourTide", method = RequestMethod.POST)
    public MarketingAnalyResult tuangouLiuliangHourTide() {
        return marketingService.tuangouLiuliangHourTide();
    }

    @RequestMapping(value = "/miaoshaLiuliangDayTide", method = RequestMethod.POST)
    public MarketingAnalyResult miaoshaLiuliangDayTide() {
        return marketingService.miaoshaLiuliangDayTide();
    }

    @RequestMapping(value = "/miaoshaLiuliangHourTide", method = RequestMethod.POST)
    public MarketingAnalyResult miaoshaLiuliangHourTide() {
        return marketingService.miaoshaLiuliangHourTide();
    }

    @RequestMapping(value = "/huodongLiuliangDayTide", method = RequestMethod.POST)
    public MarketingAnalyResult huodongLiuliangDayTide() {
        return marketingService.huodongLiuliangDayTide();
    }

    @RequestMapping(value = "/huodongLiuliangHourTide", method = RequestMethod.POST)
    public MarketingAnalyResult huodongLiuliangHourTide() {
        return marketingService.huodongLiuliangHourTide();
    }

    // ==================== 交易（成交）趋势 ====================

    @RequestMapping(value = "/miaoshaDeal", method = RequestMethod.POST)
    public MarketingAnalyResult miaoshaDeal() {
        return marketingService.miaoshaDeal();
    }

    @RequestMapping(value = "/tuangouDealDayTide", method = RequestMethod.POST)
    public MarketingAnalyResult tuangouDealDayTide() {
        return marketingService.tuangouDealDayTide();
    }

    @RequestMapping(value = "/tuangouDealHourTide", method = RequestMethod.POST)
    public MarketingAnalyResult tuangouDealHourTide() {
        return marketingService.tuangouDealHourTide();
    }

    @RequestMapping(value = "/miaoshaDealDayTide", method = RequestMethod.POST)
    public MarketingAnalyResult miaoshaDealDayTide() {
        return marketingService.miaoshaDealDayTide();
    }

    @RequestMapping(value = "/miaoshaDealHourTide", method = RequestMethod.POST)
    public MarketingAnalyResult miaoshaDealHourTide() {
        return marketingService.miaoshaDealHourTide();
    }

    @RequestMapping(value = "/huodongTrend", method = RequestMethod.POST)
    public MarketingAnalyResult huodongTrend() {
        return marketingService.huodongTrend();
    }

    @RequestMapping(value = "/huodongDealDayTide", method = RequestMethod.POST)
    public MarketingAnalyResult huodongDealDayTide() {
        return marketingService.huodongDealDayTide();
    }

    @RequestMapping(value = "/huodongDealHourTide", method = RequestMethod.POST)
    public MarketingAnalyResult huodongDealHourTide() {
        return marketingService.huodongDealHourTide();
    }

    @RequestMapping(value = "/dealTrend", method = RequestMethod.POST)
    public MarketingAnalyResult dealTrend() {
        return marketingService.dealTrend();
    }
}
