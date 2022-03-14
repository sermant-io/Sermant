/*
 * Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package com.huawei.sermant.core.service.tracing;

import com.huawei.sermant.core.service.tracing.common.SpanEvent;

/**
 * 将Span上下文注入载体的函数式接口
 *
 * @param <T>
 * @author luanwenfei
 * @since 2022-02-28
 */
@FunctionalInterface
public interface InjectService<T> {
    /**
     * 跨进程链路追踪，需要将SpanContext内容放入协议载体，
     * TraceId->TRACE_ID、ParentSpanId->PARENT_SPAN_ID、NextSpanIdPrefix->SPAN_ID_PREFIX为必选项
     *
     * @param spanEvent span信息
     * @param carrier SpanContext携带载体
     */
    void addToCarrier(SpanEvent spanEvent, T carrier);
}
