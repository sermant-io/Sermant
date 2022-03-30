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

package com.huawei.sermant.core.service.tracing.api;

import com.huawei.sermant.core.service.tracing.common.TracingRequest;

/**
 * 将Span上下文提取出载体的函数式接口
 *
 * @param <T>
 * @author luanwenfei
 * @since 2022-02-28
 */
@FunctionalInterface
public interface ExtractService<T> {
    /**
     * 跨进程链路追踪，需要将SpanContext从协议载体中取出，
     * TRACE_ID->TraceId、PARENT_SPAN_ID->ParentSpanId、SPAN_ID_PREFIX->SpanIdPrefix为必选项
     *
     * @param tracingRequest SpanStart生命周期所需构建数据
     * @param carrier 协议载体
     */
    void getFromCarrier(TracingRequest tracingRequest, T carrier);
}
