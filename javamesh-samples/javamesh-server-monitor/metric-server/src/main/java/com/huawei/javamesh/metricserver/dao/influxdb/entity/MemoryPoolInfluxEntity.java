/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.javamesh.metricserver.dao.influxdb.entity;

import com.influxdb.annotations.Column;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Memory Pool Influxdb持久化实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
public abstract class MemoryPoolInfluxEntity extends CommonMetricInfluxEntity {

    @Column(name = "init")
    private Long init;

    @Column(name = "max")
    private Long max;

    @Column(name = "used")
    private Long used;

    @Column(name = "committed")
    private Long committed;
}
