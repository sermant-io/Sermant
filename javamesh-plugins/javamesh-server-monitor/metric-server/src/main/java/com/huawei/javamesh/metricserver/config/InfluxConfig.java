/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.javamesh.metricserver.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Influxdb配置
 */
@Data
@ConfigurationProperties(
    prefix = "influx"
)
public class InfluxConfig {
    private String token;
    private String bucket;
    private String org;
    private String url;
}
