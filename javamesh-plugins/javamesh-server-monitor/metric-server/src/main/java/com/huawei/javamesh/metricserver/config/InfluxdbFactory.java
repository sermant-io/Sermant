/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.javamesh.metricserver.config;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * InfluxDBClient Bean Factory
 */
@Configuration
public class InfluxdbFactory {

    @Bean
    public InfluxDBClient influxDBClient(InfluxConfig config) {
        return InfluxDBClientFactory.create(config.getUrl(),
            config.getToken().toCharArray(), config.getOrg(), config.getBucket());
    }
}
