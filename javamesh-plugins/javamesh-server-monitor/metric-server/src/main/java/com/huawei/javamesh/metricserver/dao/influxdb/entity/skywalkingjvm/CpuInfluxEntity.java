/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.javamesh.metricserver.dao.influxdb.entity.skywalkingjvm;

import com.huawei.javamesh.metricserver.dao.influxdb.entity.CommonMetricInfluxEntity;
import com.influxdb.annotations.Column;
import com.influxdb.annotations.Measurement;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Oracle jvm metric cpu Influxdb持久化实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Measurement(name = "oracle_jvm_monitor_cpu")
public class CpuInfluxEntity extends CommonMetricInfluxEntity {

    @Column(name = "usage_percent")
    private Double usagePercent;
}
