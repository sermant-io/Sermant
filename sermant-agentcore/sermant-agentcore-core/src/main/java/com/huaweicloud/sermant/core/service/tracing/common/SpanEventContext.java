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

import com.huaweicloud.sermant.core.utils.StringUtils;
import com.huaweicloud.sermant.core.utils.TracingUtils;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * SpanEventContext, stores SpanEvent, Span and count
 *
 * @author luanwenfei
 * @since 2022-03-03
 */
public class SpanEventContext {
    private static final String SPAN_ID_SEPARATOR = "-";

    SpanEvent spanEvent = new SpanEvent();

    /**
     * The current span count
     */
    private AtomicInteger spanIdCount = new AtomicInteger(0);

    /**
     * The count of calls to other processes
     */
    private AtomicInteger nextSpanIdCount = new AtomicInteger(0);

    /**
     * Create SpanEventContext with TracingRequest
     *
     * @param tracingRequest transmit span information
     */
    public SpanEventContext(TracingRequest tracingRequest) {
        checkAndSetTraceId(tracingRequest.getTraceId());
        checkAndSetSpanId(tracingRequest.getSpanIdPrefix());
        this.spanEvent.setParentSpanId(tracingRequest.getParentSpanId());
        this.spanEvent.setSpanIdPrefix(tracingRequest.getSpanIdPrefix());
        this.spanEvent.setClassName(tracingRequest.getClassName());
        this.spanEvent.setMethod(tracingRequest.getMethod());
        this.spanEvent.setSourceInfo(tracingRequest.getSourceInfo());
    }

    private void checkAndSetTraceId(String traceId) {
        // Check Whether data is a transparent link or a new link. If the traceId is empty, create a link
        // and generate a traceId
        if (StringUtils.isBlank(traceId)) {
            this.spanEvent.setTraceId(TracingUtils.generateTraceId());
        } else {
            this.spanEvent.setTraceId(traceId);
        }
    }

    private void checkAndSetSpanId(String spanIdPrefix) {
        // Check whether the SpanId prefix is empty. If no prefix exists, it indicates that the current depth is
        // layer 1 and no prefix needs to be added
        if (StringUtils.isBlank(spanIdPrefix)) {
            this.spanEvent.setSpanId(String.valueOf(this.spanIdCount.getAndIncrement()));
        } else {
            this.spanEvent.setSpanId(spanIdPrefix + SPAN_ID_SEPARATOR + this.spanIdCount.getAndIncrement());
        }
    }

    /**
     * All the spans added when non-onEntry events are triggered are sub-spans
     */
    public void addChildrenSpan() {
        this.spanEvent = new SpanEvent(this.spanEvent);
        if (StringUtils.isBlank(this.spanEvent.getSpanIdPrefix())) {
            this.spanEvent.setSpanId(String.valueOf(this.spanIdCount.getAndIncrement()));
            return;
        }
        this.spanEvent
                .setSpanId(this.spanEvent.getSpanIdPrefix() + SPAN_ID_SEPARATOR + this.spanIdCount.getAndIncrement());
    }

    /**
     * Configure the spanIdPrefix of the next process
     */
    public void configNextSpanIdPrefix() {
        this.spanEvent.setNextSpanIdPrefix(
                this.spanEvent.getSpanId() + SPAN_ID_SEPARATOR + this.nextSpanIdCount.getAndIncrement());
    }

    public SpanEvent getSpanEvent() {
        return spanEvent;
    }

    public void setSpanEvent(SpanEvent spanEvent) {
        this.spanEvent = spanEvent;
    }

    public AtomicInteger getSpanIdCount() {
        return spanIdCount;
    }

    public void setSpanIdCount(AtomicInteger spanIdCount) {
        this.spanIdCount = spanIdCount;
    }

    public AtomicInteger getNextSpanIdCount() {
        return nextSpanIdCount;
    }

    public void setNextSpanIdCount(AtomicInteger nextSpanIdCount) {
        this.nextSpanIdCount = nextSpanIdCount;
    }
}
