package cn.rp.control;

import cn.rp.result.AbTestResult;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Rp
 * @date 2026.6.12 14:42
 **/


@Controller
@CrossOrigin
public class AbTestControl {
    @RequestMapping(value = "/pvTotalDayTide",method = RequestMethod.POST)
    public AbTestResult pvTotalDayTide(){
        List<String> xList = new ArrayList<>();
        xList.add("20260612");
        xList.add("20260913");
        xList.add("20260914");
        xList.add("20260915");
        xList.add("20260916");
        xList.add("20260917");


        List<Map<String, Object>> yData = new ArrayList<Map<String,Object>>();
        List<Long> datalist = new ArrayList<Long>();

        datalist.add(45l);
        datalist.add(56l);
        datalist.add(25l);
        datalist.add(35l);
        datalist.add(16l);

        Map<String,Object> map = new HashMap<String,Object>();
        map.put("name","A版本");
        map.put("data",datalist);



        datalist.add(55l);
        datalist.add(76l);
        datalist.add(55l);
        datalist.add(95l);
        datalist.add(46l);

        map = new HashMap<String,Object>();
        map.put("name","B版本");
        map.put("data",datalist);

        AbTestResult abTestResult = new AbTestResult();
        abTestResult.setXList(xList);
        abTestResult.setYData(yData);

        return abTestResult;
    }

}