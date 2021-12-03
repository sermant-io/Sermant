/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.javamesh.metricserver.dto.servermonitor;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Builder
@Data
public class NetworkDTO {
    private final String service;
    private final String serviceInstance;
    private final Instant time;

    /**
     * 采集周期内的每秒读字节数
     */
    private final Long readBytesPerSec;

    /**
     * 采集周期内的每秒写字节数
     */
    private final Long writeBytesPerSec;

    /**
     * 采集周期内的每秒读包数
     */
    private final Long readPackagesPerSec;

    /**
     * 采集周期内的每秒写包数
     */
    private final Long writePackagesPerSec;

}
