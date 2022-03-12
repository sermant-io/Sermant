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

package com.huawei.sermant.core.service.tracing.sender;

import com.huawei.sermant.core.common.LoggerFactory;
import com.huawei.sermant.core.service.ServiceManager;
import com.huawei.sermant.core.service.send.GatewayClient;
import com.huawei.sermant.core.service.tracing.common.SpanEvent;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;

import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

/**
 * 链路追踪消息发送器
 *
 * @author luanwenfei
 * @since 2022-03-04
 */
public class TracingSender {
    private static final Logger LOGGER = LoggerFactory.getLogger();

    private static final int TRACING_DATA_TYPE = 10;

    private static final int MAX_SPAN_EVENT_COUNT = 512;

    private static final long TRACING_SENDER_MINIMAL_INTERVAL = 1000L;

    private static final long STOP_TIME_OUT = 3000L;

    private static final ArrayBlockingQueue<SpanEvent> SPAN_EVENT_DATA_QUEUE =
        new ArrayBlockingQueue<>(MAX_SPAN_EVENT_COUNT);

    private static final ExecutorService EXECUTOR =
        Executors.newSingleThreadExecutor(runnable -> new Thread(runnable, "tracing-sender-thread"));

    private static TracingSender tracingSender = null;

    private GatewayClient gatewayClient;

    /**
     * 当前服务开启和关闭的标记位
     */
    private boolean isSending;

    private TracingSender() {
    }

    /**
     * 获取TracingSender单例
     *
     * @return TracingSender单例
     */
    public static synchronized TracingSender getInstance() {
        if (tracingSender == null) {
            tracingSender = new TracingSender();
        }
        return tracingSender;
    }

    /**
     * Starting tracingSender.
     */
    public void start() {
        if (this.isSending) {
            LOGGER.info("TracingSender has started.");
            return;
        }
        this.isSending = true;
        gatewayClient = ServiceManager.getService(GatewayClient.class);
        EXECUTOR.execute(new SpanEventSendThread());
    }

    /**
     * Stopping tracingSender.
     */
    public void stop() {
        if (!this.isSending) {
            LOGGER.info("TracingSender has stopped.");
        }
        stopSoft(STOP_TIME_OUT);
        this.isSending = false;
    }

    /**
     * 检查阻塞队列、阻塞队列为空或者等待超时后关闭
     *
     * @param timeOut 超时时间
     */
    public void stopSoft(long timeOut) {
        long timeDuring = 0L;
        while (!SPAN_EVENT_DATA_QUEUE.isEmpty() && timeDuring < timeOut) {
            try {
                Thread.sleep(TRACING_SENDER_MINIMAL_INTERVAL);
                timeDuring += TRACING_SENDER_MINIMAL_INTERVAL;
            } catch (InterruptedException e) {
                LOGGER.severe(String.format(Locale.ROOT,
                    "Exception [%s] occurs for [%s] when waiting to stop TracingSender service. ", e.getClass(),
                    e.getMessage()));
            }
        }
        SPAN_EVENT_DATA_QUEUE.clear();
        this.isSending = false;
    }

    /**
     * 向阻塞队列里添加SpanEvent 发送数据线程获取后发送到backend
     *
     * @param spanEvent span数据
     */
    public void offerSpanEvent(SpanEvent spanEvent) {
        if (spanEvent == null) {
            return;
        }
        boolean isSuccessful = SPAN_EVENT_DATA_QUEUE.offer(spanEvent);
        if (!isSuccessful) {
            LOGGER.warning("Failed to offer spanEvent.");
        }
    }

    /**
     * 链路追踪消息发送线程
     *
     * @author luanwenfei
     * @since 2022-03-04
     */
    private class SpanEventSendThread extends Thread {
        @Override
        public void run() {
            LOGGER.info("TracingSender started.");
            while (isSending) {
                Optional<TracingMessage> tracingMessage = buildTracingMessage();
                if (!tracingMessage.isPresent()) {
                    // 如果没有获取到SpanEvent,等待一段时间后再次执行
                    try {
                        Thread.sleep(TRACING_SENDER_MINIMAL_INTERVAL);
                    } catch (InterruptedException e) {
                        LOGGER.severe(String.format(Locale.ROOT, "Exception [%s] occurs for [%s] when waiting queue. ",
                            e.getClass(), e.getMessage()));
                    }
                    continue;
                }
                sendMessage(tracingMessage.get());
            }
            LOGGER.info("TracingSender stopped.");
        }

        private Optional<TracingMessage> buildTracingMessage() {
            SpanEvent spanEvent = SPAN_EVENT_DATA_QUEUE.poll();
            if (spanEvent == null) {
                LOGGER.warning("SpanEvent is null.");
                return Optional.empty();
            }

            // 节点信息待整改配置后获取
            TracingMessageHeader tracingMessageHeader = TracingMessageHeader.builder().build();
            return Optional.of(new TracingMessage(spanEvent.getTraceId(), tracingMessageHeader, spanEvent));
        }

        private void sendMessage(TracingMessage tracingMessage) {
            LOGGER.info(String.format(Locale.ROOT, "Sending tracing message traceId : [%s] , spanId : [%s] .",
                tracingMessage.getBody().getTraceId(), tracingMessage.getBody().getSpanId()));
            String serializedMessage = JSON.toJSONString(tracingMessage, SerializerFeature.WriteMapNullValue);
            gatewayClient.send(serializedMessage.getBytes(StandardCharsets.UTF_8), TRACING_DATA_TYPE);
        }
    }
}
