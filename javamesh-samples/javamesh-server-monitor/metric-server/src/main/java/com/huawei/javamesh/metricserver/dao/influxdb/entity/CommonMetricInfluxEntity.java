/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.javamesh.metricserver.dao.influxdb.entity;

import com.influxdb.annotations.Column;
import lombok.Data;

import java.time.Instant;

/**
 * 通用Influxdb持久化实体
 */
@Data
public abstract class CommonMetricInfluxEntity {
    @Column(timestamp = true)
    private Instant time;

    @Column(tag = true, name = "service")
    private String service;

    @Column(tag = true, name = "service_instance")
    private String serviceInstance;
}
