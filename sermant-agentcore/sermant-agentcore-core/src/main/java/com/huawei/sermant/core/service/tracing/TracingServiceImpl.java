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

import com.huawei.sermant.core.common.LoggerFactory;
import com.huawei.sermant.core.service.tracing.common.SpanEvent;
import com.huawei.sermant.core.service.tracing.common.SpanEventContext;
import com.huawei.sermant.core.service.tracing.common.TracingRequest;
import com.huawei.sermant.core.service.tracing.sender.TracingSender;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;

import java.util.Locale;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * 链路追踪接口的实现
 *
 * @author luanwenfei
 * @since 2022-03-01
 */
public class TracingServiceImpl implements TracingService {
    private static final Logger LOGGER = LoggerFactory.getLogger();

    /**
     * 每级最大span event个数
     */
    private static final int MAX_SPAN_EVENT_COUNT = 500;

    /**
     * 采样深度
     */
    private static final int MAX_SPAN_EVENT_DEPTH = 100;

    private final TracingSender tracingSender = TracingSender.getInstance();

    private final ThreadLocal<SpanEventContext> threadLocal = new ThreadLocal<>();

    /**
     * 链路采集开关标志位
     */
    private boolean isTracing;

    @Override
    public void start() {
        this.isTracing = true;
        tracingSender.start();
        LOGGER.info("TracingService started.");
    }

    @Override
    public void stop() {
        this.isTracing = false;
        tracingSender.stop();
        LOGGER.info("TracingService stopped.");
    }

    @Override
    public <T> Optional<SpanEvent> onProviderSpanStart(TracingRequest tracingRequest, ExtractService<T> extractService,
        T carrier) {
        if (!isTracing) {
            return Optional.empty();
        }

        // 从协议载体中获取TraceId、SpanId等数据
        extractService.getFromCarrier(tracingRequest, carrier);
        if (!filterSpanDepth(tracingRequest)) {
            return Optional.empty();
        }
        long startTime = System.currentTimeMillis();
        SpanEventContext spanEventContext = new SpanEventContext(tracingRequest);
        SpanEvent spanEvent = spanEventContext.getSpanEvent();
        spanEvent.setStartTime(startTime);
        threadLocal.set(spanEventContext);
        return Optional.of(spanEvent);
    }

    @Override
    public Optional<SpanEvent> onNormalSpanStart(TracingRequest tracingRequest) {
        if (!isTracing) {
            return Optional.empty();
        }
        Optional<SpanEvent> spanEventOptional = configureSpanEvent(tracingRequest);
        if (!spanEventOptional.isPresent()) {
            return spanEventOptional;
        }
        SpanEvent spanEvent = spanEventOptional.get();
        return Optional.of(spanEvent);
    }

    @Override
    public <T> Optional<SpanEvent> onConsumerSpanStart(TracingRequest tracingRequest, InjectService<T> injectService,
        T carrier) {
        if (!isTracing) {
            return Optional.empty();
        }
        Optional<SpanEvent> spanEventOptional = configureSpanEvent(tracingRequest);
        if (!spanEventOptional.isPresent()) {
            return spanEventOptional;
        }
        SpanEvent spanEvent = spanEventOptional.get();
        SpanEventContext spanEventContext = threadLocal.get();
        spanEventContext.configNextSpanIdPrefix();
        injectService.addToCarrier(spanEvent, carrier);
        return Optional.of(spanEvent);
    }

    private Optional<SpanEvent> configureSpanEvent(TracingRequest tracingRequest) {
        SpanEventContext spanEventContext = threadLocal.get();

        // 当前Span个数已经超过当前层能采集的最大值，需要清空ThreadLocal不再采集，防止内存泄露
        if (spanEventContext == null || spanEventContext.getSpanIdCount().get() > MAX_SPAN_EVENT_COUNT) {
            threadLocal.remove();
            return Optional.empty();
        }
        spanEventContext.addChildrenSpan();
        SpanEvent spanEvent = spanEventContext.getSpanEvent();
        spanEvent.setStartTime(System.currentTimeMillis());
        spanEvent.setClassName(tracingRequest.getClassName());
        spanEvent.setMethod(tracingRequest.getMethod());
        return Optional.of(spanEvent);
    }

    @Override
    public void onSpanFinally() {
        if (!isTracing) {
            return;
        }
        SpanEventContext spanEventContext = threadLocal.get();
        if (spanEventContext == null) {
            return;
        }
        SpanEvent spanEvent = spanEventContext.getSpanEvent();
        spanEvent.setEndTime(System.currentTimeMillis());
        sendSpanEvent(spanEvent);

        // 发送完SpanEvent数据后，需要将当前上下文中存放的SpanEvent置为当前Span的父Span
        if (spanEvent.getParentSpan() != null) {
            spanEventContext.setSpanEvent(spanEvent.getParentSpan());
        }
    }

    @Override
    public Optional<SpanEvent> onSpanError(Throwable throwable) {
        if (!isTracing) {
            return Optional.empty();
        }
        SpanEventContext spanEventContext = threadLocal.get();
        if (spanEventContext == null) {
            return Optional.empty();
        }
        SpanEvent spanEvent = spanEventContext.getSpanEvent();
        spanEvent.setError(true);
        spanEvent.setErrorInfo(throwable.getMessage());
        return Optional.of(spanEvent);
    }

    /**
     * 通过SpanId来限制采样深度
     *
     * @param tracingRequest 调用链路追踪生命周期时需要传入的参数
     */
    private boolean filterSpanDepth(TracingRequest tracingRequest) {
        // 检查spanId长度 大于100忽略
        String spanIdPrefix = tracingRequest.getSpanIdPrefix();
        if (spanIdPrefix != null && spanIdPrefix.length() > MAX_SPAN_EVENT_DEPTH) {
            LOGGER.info(String.format(Locale.ROOT, "SpanId is too long, discard this span : [%s]",
                JSON.toJSONString(tracingRequest, SerializerFeature.WriteMapNullValue)));
            return false;
        }
        return true;
    }

    private void sendSpanEvent(SpanEvent spanEvent) {
        tracingSender.offerSpanEvent(spanEvent);
    }
}
