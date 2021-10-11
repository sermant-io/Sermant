package org.apache.dubbo.demo;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DemoApp implements ApplicationRunner {

    public static void main(String[] args) {
        SpringApplication.run(DemoApp.class, args);
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
    }
}
