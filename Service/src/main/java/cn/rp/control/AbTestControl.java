package cn.rp.control;

import cn.rp.param.AbTestVo;
import cn.rp.result.AbTestResult;
import cn.rp.service.AbTestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * AB 测试数据接口控制器
 *
 * @author Rp
 * @date 2026.6.21 23:02
 **/
@RestController
public class AbTestControl {

    @Autowired
    private AbTestService abTestService;

    // ==================== 总体趋势 ====================

    @RequestMapping(value = "/pvTotalDayTide", method = RequestMethod.POST)
    public AbTestResult pvTotalDayTide() {
        return abTestService.pvTotalDayTide();
    }

    @RequestMapping(value = "/uvTotalDayTide", method = RequestMethod.POST)
    public AbTestResult uvTotalDayTide() {
        return abTestService.uvTotalDayTide();
    }

    @RequestMapping(value = "/pvTotalHourTide", method = RequestMethod.POST)
    public AbTestResult pvTotalHourTide() {
        return abTestService.pvTotalHourTide();
    }

    @RequestMapping(value = "/uvTotalHourTide", method = RequestMethod.POST)
    public AbTestResult uvTotalHourTide() {
        return abTestService.uvTotalHourTide();
    }

    // ==================== PV 明细 ====================

    @RequestMapping(value = "/pvDayDetail", method = RequestMethod.POST)
    public AbTestResult pvDayDetail(@RequestBody AbTestVo abTestVo) {
        return abTestService.pvDayDetail(abTestVo);
    }

    @RequestMapping(value = "/pvHourDetail", method = RequestMethod.POST)
    public AbTestResult pvHourDetail(@RequestBody AbTestVo abTestVo) {
        return abTestService.pvHourDetail(abTestVo);
    }

    @RequestMapping(value = "/pvDayproductLeave", method = RequestMethod.POST)
    public AbTestResult pvDayproductLeave(@RequestBody AbTestVo abTestVo) {
        return abTestService.pvDayproductLeave(abTestVo);
    }

    @RequestMapping(value = "/pvHourproductLeave", method = RequestMethod.POST)
    public AbTestResult pvHourproductLeave(@RequestBody AbTestVo abTestVo) {
        return abTestService.pvHourproductLeave(abTestVo);
    }

    @RequestMapping(value = "/pvDayproductType", method = RequestMethod.POST)
    public AbTestResult pvDayproductType(@RequestBody AbTestVo abTestVo) {
        return abTestService.pvDayproductType(abTestVo);
    }

    @RequestMapping(value = "/pvHourproductType", method = RequestMethod.POST)
    public AbTestResult pvHourproductType(@RequestBody AbTestVo abTestVo) {
        return abTestService.pvHourproductType(abTestVo);
    }

    // ==================== UV 明细 ====================

    @RequestMapping(value = "/uvDayDetail", method = RequestMethod.POST)
    public AbTestResult uvDayDetail(@RequestBody AbTestVo abTestVo) {
        return abTestService.uvDayDetail(abTestVo);
    }

    @RequestMapping(value = "/uvHourDetail", method = RequestMethod.POST)
    public AbTestResult uvHourDetail(@RequestBody AbTestVo abTestVo) {
        return abTestService.uvHourDetail(abTestVo);
    }

    @RequestMapping(value = "/uvDayproductLeave", method = RequestMethod.POST)
    public AbTestResult uvDayproductLeave(@RequestBody AbTestVo abTestVo) {
        return abTestService.uvDayproductLeave(abTestVo);
    }

    @RequestMapping(value = "/uvHourproductLeave", method = RequestMethod.POST)
    public AbTestResult uvHourproductLeave(@RequestBody AbTestVo abTestVo) {
        return abTestService.uvHourproductLeave(abTestVo);
    }

    @RequestMapping(value = "/uvDayproductType", method = RequestMethod.POST)
    public AbTestResult uvDayproductType(@RequestBody AbTestVo abTestVo) {
        return abTestService.uvDayproductType(abTestVo);
    }

    @RequestMapping(value = "/uvHourproductType", method = RequestMethod.POST)
    public AbTestResult uvHourproductType(@RequestBody AbTestVo abTestVo) {
        return abTestService.uvHourproductType(abTestVo);
    }
}
