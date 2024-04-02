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

package com.huaweicloud.sermant.core.service.tracing.api;

import com.huaweicloud.sermant.core.service.tracing.common.TracingRequest;

/**
 * Functional interface for extracting Span from a carrier
 *
 * @param <T>
 * @author luanwenfei
 * @since 2022-02-28
 */
@FunctionalInterface
public interface ExtractService<T> {
    /**
     * For cross-process tracing, the SpanContext needs to be extracted from the protocol carrier,
     * TRACE_ID->TraceId、PARENT_SPAN_ID->ParentSpanId、SPAN_ID_PREFIX->SpanIdPrefix required
     *
     * @param tracingRequest SpanStart required build data
     * @param carrier protocol carrier
     */
    void getFromCarrier(TracingRequest tracingRequest, T carrier);
}
