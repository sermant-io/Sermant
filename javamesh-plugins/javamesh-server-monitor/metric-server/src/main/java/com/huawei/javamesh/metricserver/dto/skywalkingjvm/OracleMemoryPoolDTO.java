/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.javamesh.metricserver.dto.skywalkingjvm;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Builder
@Data
public class OracleMemoryPoolDTO {
    private final String service;
    private final String serviceInstance;
    private final Instant time;

    private final OraclePoolType type;
    private final Long init;
    private final Long max;
    private final Long used;
    private final Long committed;

    public enum OraclePoolType {
        CODE_CACHE,
        NEW_GEN,
        OLD_GEN,
        PERM_GEN,
        SURVIVOR,
        METASPACE
    }
}
