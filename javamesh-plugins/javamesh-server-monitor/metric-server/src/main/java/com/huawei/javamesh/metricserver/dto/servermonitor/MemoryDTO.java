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

@Builder
@Data
public class MemoryDTO {
    private final String service;
    private final String serviceInstance;
    private final Instant time;

    /**
     * 总内存大小
     */
    private final Long memoryTotal;

    /**
     * 已使用的内存大小
     */
    private final Long swapCached;

    /**
     * 对应cat /proc/meminfo指令的Cached
     */
    private final Long cached;

    /**
     * 对应cat /proc/meminfo指令的Buffers
     */
    private final Long buffers;

    /**
     * 对应cat /proc/meminfo指令的SwapCached
     */
    private final Long memoryUsed;

}
