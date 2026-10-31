package cn.rp.control;

import cn.rp.result.TerminalAnalyResult;
import cn.rp.service.TerminalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * 终端分析数据接口
 *
 * @author Rp
 * @date 2026.6.23 18:14
 **/
@RestController
@CrossOrigin
public class TerminalControl {

    @Autowired
    private TerminalService terminalService;

    @RequestMapping(value = "/terminalTrafficDayTide", method = RequestMethod.POST)
    public TerminalAnalyResult terminalTrafficDayTide() {
        return terminalService.terminalTrafficDayTide();
    }

    @RequestMapping(value = "/terminalTrafficHourTide", method = RequestMethod.POST)
    public TerminalAnalyResult terminalTrafficHourTide() {
        return terminalService.terminalTrafficHourTide();
    }

    @RequestMapping(value = "/terminalUserDayTide", method = RequestMethod.POST)
    public TerminalAnalyResult terminalUserDayTide() {
        return terminalService.terminalUserDayTide();
    }

    @RequestMapping(value = "/terminalUserHourTide", method = RequestMethod.POST)
    public TerminalAnalyResult terminalUserHourTide() {
        return terminalService.terminalUserHourTide();
    }

    // 兼容旧名
    @RequestMapping(value = "/terminalTraffic", method = RequestMethod.POST)
    public TerminalAnalyResult terminalTraffic() {
        return terminalService.terminalTrafficHourTide();
    }

    @RequestMapping(value = "/terminalUserTrend", method = RequestMethod.POST)
    public TerminalAnalyResult terminalUserTrend() {
        return terminalService.terminalUserHourTide();
    }
}
