/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.javamesh.metricserver.dao.influxdb.entity.servermonitor;

import com.huawei.javamesh.metricserver.dao.influxdb.entity.CommonMetricInfluxEntity;
import com.influxdb.annotations.Column;
import com.influxdb.annotations.Measurement;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * server monitor memory Influxdb持久化实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Measurement(name = "server_monitor_memory")
public class MemoryInfluxEntity extends CommonMetricInfluxEntity {
    /**
     * 总内存大小
     */
    @Column(name = "memory_total")
    private Long memoryTotal;

    /**
     * 已使用的内存大小
     */
    @Column(name = "swap_cached")
    private Long swapCached;

    /**
     * 对应cat /proc/meminfo指令的Cached
     */
    @Column(name ="cached")
    private Long cached;

    /**
     * 对应cat /proc/meminfo指令的Buffers
     */
    @Column(name ="buffers")
    private Long buffers;

    /**
     * 对应cat /proc/meminfo指令的SwapCached
     */
    @Column(name ="memory_used")
    private Long memoryUsed;
}
