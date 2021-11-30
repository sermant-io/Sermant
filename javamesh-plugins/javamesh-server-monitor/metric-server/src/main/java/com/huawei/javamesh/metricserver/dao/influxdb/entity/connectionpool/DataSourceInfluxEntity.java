/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.javamesh.metricserver.dao.influxdb.entity.connectionpool;

import com.huawei.javamesh.metricserver.dao.influxdb.entity.CommonMetricInfluxEntity;
import com.influxdb.annotations.Column;
import com.influxdb.annotations.Measurement;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Druid datasource Influxdb持久化实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Measurement(name = "druid_datasource")
public class DataSourceInfluxEntity extends CommonMetricInfluxEntity {
    @Column(tag = true, name = "name")
    private String name;

    @Column(tag = true, name = "database_peer")
    private String databasePeer;

    @Column(name = "active_count")
    private Long activeCount;

    @Column(name = "pooling_count")
    private Long poolingCount;

    @Column(name = "max_active")
    private Long maxActive;

    @Column(name = "initial_size")
    private Long initialSize;
}
