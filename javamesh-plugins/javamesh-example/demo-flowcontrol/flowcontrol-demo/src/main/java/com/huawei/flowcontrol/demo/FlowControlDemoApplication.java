package com.huawei.flowcontrol.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 流控测试Demo
 *
 * @author zhouss
 * @since 2021-11-29
 */
@SpringBootApplication
@RestController
public class FlowControlDemoApplication {
    public static void main(String[] args) {
        SpringApplication.run(FlowControlDemoApplication.class);
    }

    @GetMapping("/flow")
    public String flow() {
        return "I am flow";
    }

    @GetMapping("/degrade")
    public String degrade() throws InterruptedException {
        Thread.sleep(101);
        return "I am degrader";
    }
}
