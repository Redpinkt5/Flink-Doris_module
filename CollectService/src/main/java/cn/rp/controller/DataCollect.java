package cn.rp.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

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
                kafkaTemplate.send("datacoll", data);

        }
}