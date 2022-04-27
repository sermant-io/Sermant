/*
 * Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huaweicloud.sermant.core.service.tracing.common;

/**
 * 链路追踪插入Carrier的Key
 *
 * @author luanwenfei
 * @since 2022-03-18
 */
public enum TracingHeader {
    /**
     * 标识一整条链路
     */
    TRACE_ID("sermant-trace-id"),
    /**
     * 标识父SpanId
     */
    PARENT_SPAN_ID("sermant-parent-span-id"),
    /**
     * 标识下一进程的SpanId生成前缀
     */
    SPAN_ID_PREFIX("sermant-span-id-prefix");

    private final String value;

    TracingHeader(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
