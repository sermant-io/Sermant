/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.javamesh.metricserver.dto.servermonitor;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Builder
@Data
public class MemoryDTO {
    private final String service;
    private final String serviceInstance;
    private final Instant time;

    /**
     * 总内存大小
     */
    private final Long memoryTotal;

    /**
     * 已使用的内存大小
     */
    private final Long swapCached;

    /**
     * 对应cat /proc/meminfo指令的Cached
     */
    private final Long cached;

    /**
     * 对应cat /proc/meminfo指令的Buffers
     */
    private final Long buffers;

    /**
     * 对应cat /proc/meminfo指令的SwapCached
     */
    private final Long memoryUsed;

}
