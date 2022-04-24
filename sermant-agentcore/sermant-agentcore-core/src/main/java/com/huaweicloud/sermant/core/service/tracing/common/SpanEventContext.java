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

import com.huaweicloud.sermant.core.utils.TracingUtils;

import org.apache.commons.lang.StringUtils;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 存放SpanEvent、Span计数器及其分支计数器
 *
 * @author luanwenfei
 * @since 2022-03-03
 */
public class SpanEventContext {
    private static final String SPAN_ID_SEPARATOR = "-";

    SpanEvent spanEvent = new SpanEvent();

    /**
     * 当前已有span的计数
     */
    private AtomicInteger spanIdCount = new AtomicInteger(0);

    /**
     * 向其他进程调用的计数
     */
    private AtomicInteger nextSpanIdCount = new AtomicInteger(0);

    /**
     * 通过TracingRequest创建SpanEventContext
     *
     * @param tracingRequest 传递Span信息
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
        // 校验数据为透传链路 还是 新建链路，如果traceId为空则需要新建链路并生成traceId
        if (StringUtils.isBlank(traceId)) {
            this.spanEvent.setTraceId(TracingUtils.generateTraceId());
        } else {
            this.spanEvent.setTraceId(traceId);
        }
    }

    private void checkAndSetSpanId(String spanIdPrefix) {
        // 校验SpanId前缀是否为空，如果没有前缀则代表当前深度为第一层需要不需要添加前缀
        if (StringUtils.isBlank(spanIdPrefix)) {
            this.spanEvent.setSpanId(String.valueOf(this.spanIdCount.getAndIncrement()));
        } else {
            this.spanEvent.setSpanId(spanIdPrefix + SPAN_ID_SEPARATOR + this.spanIdCount.getAndIncrement());
        }
    }

    /**
     * 当触发非onEntry事件时添加的span均为子span
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
     * 配置下一进程的spanIdPrefix
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
