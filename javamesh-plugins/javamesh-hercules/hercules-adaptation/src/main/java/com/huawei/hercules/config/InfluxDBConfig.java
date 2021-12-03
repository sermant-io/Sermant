/*
 * Copyright (C) Huawei Technologies Co., Ltd. $YEAR$-$YEAR$. All rights reserved
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
