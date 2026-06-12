package cn.rp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

/**
 * @author Rp
 * @date 2026.5.27 17:01
 **/
@SpringBootApplication
@EnableEurekaServer
public class SpringBootRegister {
    public static void main(String[] args) {
        SpringApplication.run(SpringBootRegister.class, args);
    }

}