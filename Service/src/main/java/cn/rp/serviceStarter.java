package cn.rp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

/**
 * @author Rp
 * @date 2026.5.27 17:34
 **/
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class serviceStarter {
    public static void main(String[] args) {
        SpringApplication.run(serviceStarter.class, args);
    }
}