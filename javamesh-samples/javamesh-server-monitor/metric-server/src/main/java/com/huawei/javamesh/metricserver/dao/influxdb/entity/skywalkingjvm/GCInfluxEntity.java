/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.javamesh.metricserver.dao.influxdb.entity.skywalkingjvm;

import com.huawei.javamesh.metricserver.dao.influxdb.entity.CommonMetricInfluxEntity;
import com.influxdb.annotations.Column;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Oracle jvm metric gc Influxdb持久化实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
public abstract class GCInfluxEntity extends CommonMetricInfluxEntity {

    @Column(name = "gc_count")
    private Long gcCount;

    @Column(name = "gc_time")
    private Long gcTime;
}
