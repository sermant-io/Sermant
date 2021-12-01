/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.hercules.service.influxdb.measurement.skywalkingjvm;

import com.huawei.hercules.service.influxdb.measurement.CommonMetricInfluxEntity;
import com.influxdb.annotations.Column;

/**
 * Oracle jvm metric gc Influxdb持久化实体
 */
public abstract class GCInfluxEntity extends CommonMetricInfluxEntity {

    @Column(name = "gc_count")
    private Long gcCount;

    @Column(name = "gc_time")
    private Long gcTime;
}
