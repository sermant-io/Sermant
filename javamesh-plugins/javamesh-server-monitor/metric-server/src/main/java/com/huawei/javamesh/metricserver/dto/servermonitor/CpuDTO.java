/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.javamesh.metricserver.dto.servermonitor;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Builder
@Data
public class CpuDTO {
    private final String service;
    private final String serviceInstance;
    private final Instant time;

    /**
     * idle时间百分占比
     */
    private final Long idlePercentage;

    /**
     * io wait时间百分占比
     */
    private final Long ioWaitPercentage;

    /**
     * sys时间百分占比
     */
    private final Long sysPercentage;

    /**
     * user和nice时间百分占比
     */
    private final Long userPercentage;


}
