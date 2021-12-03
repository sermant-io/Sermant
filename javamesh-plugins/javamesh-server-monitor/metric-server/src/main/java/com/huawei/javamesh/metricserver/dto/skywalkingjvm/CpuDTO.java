/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.javamesh.metricserver.dto.skywalkingjvm;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class CpuDTO {
    private final String service;
    private final String serviceInstance;
    private final Instant time;

    private Double usagePercent;
}
