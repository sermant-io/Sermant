/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.hercules.service.influxdb.measurement.servermonitor;

import com.huawei.hercules.service.influxdb.measurement.CommonMetricInfluxEntity;
import com.influxdb.annotations.Column;
import com.influxdb.annotations.Measurement;

/**
 * server monitor cpu Influxdb持久化实体
 */
@Measurement(name = "server_monitor_cpu")
public class CpuInfluxEntity extends CommonMetricInfluxEntity {
    /**
     * idle时间百分占比
     */
    @Column(name = "idle_percentage")
    private Long idlePercentage;

    /**
     * io wait时间百分占比
     */
    @Column(name = "io_wait_percentage")
    private Long ioWaitPercentage;

    /**
     * sys时间百分占比
     */
    @Column(name = "sys_percentage")
    private Long sysPercentage;

    /**
     * user和nice时间百分占比
     */
    @Column(name = "user_percentage")
    private Long userPercentage;
}
