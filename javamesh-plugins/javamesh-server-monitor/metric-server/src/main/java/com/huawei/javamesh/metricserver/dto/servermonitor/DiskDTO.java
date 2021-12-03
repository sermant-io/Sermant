/*
 * Copyright (C) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved
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
