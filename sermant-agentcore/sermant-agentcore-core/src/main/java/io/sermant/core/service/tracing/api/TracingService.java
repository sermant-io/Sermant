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

package io.sermant.core.service.tracing.api;

import io.sermant.core.service.BaseService;
import io.sermant.core.service.tracing.common.SpanEvent;
import io.sermant.core.service.tracing.common.SpanEventContext;
import io.sermant.core.service.tracing.common.TracingRequest;

import java.util.Optional;

/**
 * Tracing service interface
 *
 * @author luanwenfei
 * @since 2022-02-28
 */
public interface TracingService extends BaseService {
    /**
     * Used to get the SpanEventContext globally within a thread
     *
     * @return Optional< SpanEventContext >
     */
    Optional<SpanEventContext> getContext();

    /**
     * Start of work unit, for non-provider and consumer scenarios
     *
     * @param tracingRequest information needed during trace lifecycle
     * @return SpanEvent
     */
    Optional<SpanEvent> onNormalSpanStart(TracingRequest tracingRequest);

    /**
     * Start of work unit, for provider scenarios
     *
     * @param tracingRequest information needed during trace lifecycle
     * @param extractService A functional interface for extracting data from the carrier during transparent transmission
     * @param carrier carrier
     * @param <T> generic type
     * @return SpanEvent
     */
    <T> Optional<SpanEvent> onProviderSpanStart(TracingRequest tracingRequest, ExtractService<T> extractService,
            T carrier);

    /**
     * Start of work unit, for consumer scenarios
     *
     * @param tracingRequest information needed during trace lifecycle
     * @param injectService A functional interface for injecting data to the carrier during transparent transmission
     * @param carrier carrier
     * @param <T> generic type
     * @return SpanEvent
     */
    <T> Optional<SpanEvent> onConsumerSpanStart(TracingRequest tracingRequest, InjectService<T> injectService,
            T carrier);

    /**
     * End of work unit
     */
    void onSpanFinally();

    /**
     * Error period of work unit
     *
     * @param throwable throwable
     * @return SpanEvent
     */
    Optional<SpanEvent> onSpanError(Throwable throwable);
}
