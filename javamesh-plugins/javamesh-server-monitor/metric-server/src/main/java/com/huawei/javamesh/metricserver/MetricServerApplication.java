/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.javamesh.metricserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.kafka.annotation.EnableKafka;

@SpringBootApplication
@EnableKafka
@ConfigurationPropertiesScan
public class MetricServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(MetricServerApplication.class, args);
    }
}
