/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.javamesh.metricserver.dto.connectionpool;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Builder
@Data
public class DataSourceDTO {
    private final String service;
    private final String serviceInstance;
    private final Instant time;

    private final String name;
    private final String databasePeer;
    private final Long activeCount;
    private final Long poolingCount;
    private final Long maxActive;
    private final Long initialSize;
}
