package cn.rp.controller;

import cn.rp.log.ScanLog;
import cn.rp.util.HbaseUtil2;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

/**
 * @author Rp
 * @date 2026.5.28 20:35
 **/
@RestController
public class DataCollect {

        @Autowired
        private KafkaTemplate<String, String> kafkaTemplate;

        @RequestMapping(value = "dataColl", method = RequestMethod.POST)
        public void dataColl(@RequestBody String data){
                System.out.println("呵呵 ="+data);
                ScanLog scanLog = JSONObject.parseObject(data,ScanLog.class);
                int deviceType = scanLog.getDeviceType();
                if (deviceType == 1){
                        String macadress = scanLog.getMacaddress();
                    try {
                            String firstvisitTime = HbaseUtil2.getdata("pcDevice", macadress, "info", "firstvisitTime");
                            if (StringUtils.isBlank(firstvisitTime)){
                                    scanLog.setIsNewUser(true);
                                    HbaseUtil2.putdata("pcDevice", macadress, "info", "firstvisitTime", scanLog.getTime()+"");
                            }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }

                }else if (deviceType == 2){
                        String deviceId = scanLog.getDeviceId();
                        try {
                                String firstvisitTime = HbaseUtil2.getdata("mobileDevice", deviceId, "info", "firstvisitTime");
                                if (StringUtils.isBlank(firstvisitTime)){
                                        scanLog.setIsNewUser(true);
                                        HbaseUtil2.putdata("mobileDevice", deviceId, "info", "firstvisitTime", scanLog.getTime()+"");
                                }
                        } catch (IOException e) {
                                throw new RuntimeException(e);
                        }
                }else if (deviceType == 3){
                        String wechatAccount = scanLog.getWechatAccount();
                        try {
                                String firstvisitTime = HbaseUtil2.getdata("wxDevice", wechatAccount, "info", "firstvisitTime");
                                if (StringUtils.isBlank(firstvisitTime)){
                                        scanLog.setIsNewUser(true);
                                        HbaseUtil2.putdata("wxDevice", wechatAccount, "info", "firstvisitTime", scanLog.getTime()+"");
                                }
                        } catch (IOException e) {
                                throw new RuntimeException(e);
                        }
                }else {
                        System.out.println("其他");
                }

                data = JSONObject.toJSONString(scanLog);
                kafkaTemplate.send("datacoll", data);

        }
}