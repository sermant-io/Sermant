package com.huawei.hercules;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients
@SpringBootApplication
public class HerculesApplication {

    public static void main(String[] args) {
        SpringApplication.run(HerculesApplication.class, args);

    }

}
