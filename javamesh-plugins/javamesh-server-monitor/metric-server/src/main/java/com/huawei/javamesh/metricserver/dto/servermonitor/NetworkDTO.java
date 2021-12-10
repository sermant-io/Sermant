/*
 * Copyright (C) 2021-2021 Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
