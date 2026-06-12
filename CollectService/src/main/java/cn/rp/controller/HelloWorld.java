package cn.rp.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Rp
 * @date 2026.5.27 18:51
 **/
@RestController
public class HelloWorld {
    @RequestMapping("hello")
    public String helloWorld(String name) {
        return name;
    }
}