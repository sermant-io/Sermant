/*
 * Copyright (C) 2022-2024 Sermant Authors. All rights reserved.
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

package com.huaweicloud.sermant.core.service.tracing.api;

import com.huaweicloud.sermant.core.service.tracing.common.SpanEvent;

/**
 * Functional interface for injecting Span to a carrier
 *
 * @param <T>
 * @author luanwenfei
 * @since 2022-02-28
 */
@FunctionalInterface
public interface InjectService<T> {
    /**
     * For cross-process tracing, the SpanContext needs to be put into the protocol carrier.
     * TraceId->TRACE_ID、ParentSpanId->PARENT_SPAN_ID、NextSpanIdPrefix->SPAN_ID_PREFIX required
     *
     * @param spanEvent span information
     * @param carrier protocol carrier
     */
    void addToCarrier(SpanEvent spanEvent, T carrier);
}
