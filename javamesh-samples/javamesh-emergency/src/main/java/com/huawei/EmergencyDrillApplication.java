/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 启动类
 *
 * @since 2021-10-30
 **/
@SpringBootApplication(scanBasePackages = "com.huawei")
@EnableFeignClients
@EnableDiscoveryClient
@ServletComponentScan
@MapperScan(basePackages = {"com.huawei.emergency.mapper"})
public class EmergencyDrillApplication {
    public static void main(String[] args) {
        SpringApplication.run(EmergencyDrillApplication.class, args);
    }
}
