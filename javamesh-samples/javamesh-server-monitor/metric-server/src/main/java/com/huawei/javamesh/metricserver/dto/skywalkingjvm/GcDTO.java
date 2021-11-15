/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.javamesh.metricserver.dto.skywalkingjvm;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class GcDTO {
    private final String service;
    private final String serviceInstance;
    private final Instant time;

    private final GcType gcType;
    private final Long gcCount;
    private final Long gcTime;

    public enum GcType {
        YOUNG,
        OLD
    }
}
