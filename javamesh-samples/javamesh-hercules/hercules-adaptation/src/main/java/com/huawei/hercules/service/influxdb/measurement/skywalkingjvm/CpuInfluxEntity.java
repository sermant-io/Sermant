/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.hercules.service.influxdb.measurement.skywalkingjvm;

import com.huawei.hercules.service.influxdb.measurement.CommonMetricInfluxEntity;
import com.influxdb.annotations.Column;
import com.influxdb.annotations.Measurement;

/**
 * Oracle jvm metric cpu Influxdb持久化实体
 */
@Measurement(name = "oracle_jvm_monitor_cpu")
public class CpuInfluxEntity extends CommonMetricInfluxEntity {

    @Column(name = "usage_percent")
    private Double usagePercent;
}
