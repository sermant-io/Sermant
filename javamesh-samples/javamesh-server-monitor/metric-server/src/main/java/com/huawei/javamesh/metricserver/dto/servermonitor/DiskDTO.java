/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.javamesh.metricserver.dto.servermonitor;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.Map;

@Builder
@Data
public class DiskDTO {
    private String service;
    private String serviceInstance;
    private Instant time;

    private ValueType type;

    /**
     * key：磁盘名称
     * value：数值
     */
    private Map<String, Object> deviceAndValueMap;

    public enum ValueType {
        READ_RATE,
        WRITE_RATE,
        IO_SPENT_PERCENTAGE
    }
}
