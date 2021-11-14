/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.hercules.config;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PreDestroy;
import java.util.Locale;

/**
 * 功能描述：执行influxDB的Sql语句
 *
 * @author z30009938
 * @since 2021-11-14
 */
@Configuration
public class InfluxDBConfig {
    @Value("${influxdb.bucket:default}")
    private String bucket;

    @Value("${influxdb.org:public}")
    private String org;

    @Value("${influxdb.token:123456}")
    private String token;

    @Value("${influxdb.host:localhost}")
    private String host;

    @Value("${influxdb.port:8086}")
    private String port;

    private InfluxDBClient influxDBClient;

    @Bean
    public InfluxDBClient influxDBClient() {
        String url = String.format(Locale.ENGLISH, "http://%s:%s", host, port);
        influxDBClient = InfluxDBClientFactory.create(url, token.toCharArray(), org, bucket);
        return influxDBClient;
    }

    @PreDestroy
    public void destroy() {
        if (influxDBClient != null) {
            influxDBClient.close();
        }
    }
}
