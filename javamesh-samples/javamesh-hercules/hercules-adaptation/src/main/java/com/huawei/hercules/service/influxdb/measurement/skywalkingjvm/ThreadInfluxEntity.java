/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.hercules.service.influxdb.measurement.skywalkingjvm;

import com.huawei.hercules.service.influxdb.measurement.CommonMetricInfluxEntity;
import com.influxdb.annotations.Column;
import com.influxdb.annotations.Measurement;

/**
 * Oracle jvm metric thread Influxdb持久化实体
 */
@Measurement(name = "oracle_jvm_monitor_thread")
public class ThreadInfluxEntity extends CommonMetricInfluxEntity {

    @Column(name = "live_count")
    private Long liveCount;

    @Column(name = "daemon_count")
    private Long daemonCount;

    @Column(name = "peak_count")
    private Long peakCount;
}
