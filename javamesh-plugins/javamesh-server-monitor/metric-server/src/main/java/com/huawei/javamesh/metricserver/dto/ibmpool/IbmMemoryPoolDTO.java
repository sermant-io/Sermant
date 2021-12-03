/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.javamesh.metricserver.dto.ibmpool;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Builder
@Data
public class IbmMemoryPoolDTO {
    private final String service;
    private final String serviceInstance;
    private final Instant time;

    private final IbmPoolType type;
    private final Long init;
    private final Long max;
    private final Long used;
    private final Long committed;

    private final String typeDescription;
}
