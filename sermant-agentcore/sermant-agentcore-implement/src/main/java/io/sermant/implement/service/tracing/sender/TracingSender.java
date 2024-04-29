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

package io.sermant.implement.service.tracing.sender;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;

import io.sermant.core.common.LoggerFactory;
import io.sermant.core.service.ServiceManager;
import io.sermant.core.service.send.api.GatewayClient;
import io.sermant.core.service.tracing.common.SpanEvent;
import io.sermant.implement.service.send.netty.pojo.Message;

import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

/**
 * Tracing message sender
 *
 * @author luanwenfei
 * @since 2022-03-04
 */
public class TracingSender {
    private static final Logger LOGGER = LoggerFactory.getLogger();

    private static final int MAX_SPAN_EVENT_COUNT = 512;

    private static final long TRACING_SENDER_MINIMAL_INTERVAL = 1000L;

    private static final long STOP_TIME_OUT = 3000L;

    private static TracingSender tracingSender = null;

    private final ArrayBlockingQueue<SpanEvent> spanEvents;

    private ExecutorService executorService;

    private GatewayClient gatewayClient;

    /**
     * Flag that enable and disable the current service
     */
    private boolean isSending;

    private TracingSender() {
        spanEvents = new ArrayBlockingQueue<>(MAX_SPAN_EVENT_COUNT);
    }

    /**
     * Get the TracingSender singleton
     *
     * @return TracingSender singleton
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
        if (executorService == null || executorService.isShutdown()) {
            executorService = Executors.newSingleThreadExecutor(
                    runnable -> new Thread(runnable, "tracing-sender-thread"));
        }
        SpanEventSendThread spanEventSendThread = new SpanEventSendThread();
        spanEventSendThread.setName("tracing-sender-thread");
        executorService.execute(spanEventSendThread);
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
     * Check whether the block queue is empty or closes after waiting for timeout
     *
     * @param timeOut timeout
     */
    public void stopSoft(long timeOut) {
        long timeDuring = 0L;
        while (!spanEvents.isEmpty() && timeDuring < timeOut) {
            try {
                Thread.sleep(TRACING_SENDER_MINIMAL_INTERVAL);
                timeDuring += TRACING_SENDER_MINIMAL_INTERVAL;
            } catch (InterruptedException e) {
                LOGGER.severe(String.format(Locale.ROOT,
                        "Exception [%s] occurs for [%s] when waiting to stop TracingSender service. ", e.getClass(),
                        e.getMessage()));
            }
        }
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdownNow();
        }
        spanEvents.clear();
        this.isSending = false;
    }

    /**
     * Add SpanEvent to the blocking queue to send data to backend after the thread obtains it
     *
     * @param spanEvent span event
     */
    public void offerSpanEvent(SpanEvent spanEvent) {
        if (spanEvent == null) {
            return;
        }
        boolean isSuccessful = spanEvents.offer(spanEvent);
        if (!isSuccessful) {
            LOGGER.warning("Failed to offer spanEvent.");
        }
    }

    /**
     * Tracing message sending thread
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
                    // If no SpanEvent is obtained, wait for a period of time and then execute it again
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
            SpanEvent spanEvent = spanEvents.poll();
            if (spanEvent == null) {
                return Optional.empty();
            }

            // Node information needs to be obtained after the configuration is modified
            TracingMessageHeader tracingMessageHeader = new TracingMessageHeader();
            return Optional.of(new TracingMessage(spanEvent.getTraceId(), tracingMessageHeader, spanEvent));
        }

        private void sendMessage(TracingMessage tracingMessage) {
            LOGGER.info(String.format(Locale.ROOT, "Sending tracing message traceId : [%s] , spanId : [%s] .",
                    tracingMessage.getBody().getTraceId(), tracingMessage.getBody().getSpanId()));
            String serializedMessage = JSON.toJSONString(tracingMessage, SerializerFeature.WriteMapNullValue);
            gatewayClient.send(serializedMessage.getBytes(StandardCharsets.UTF_8),
                    Message.ServiceData.DataType.TRACING_DATA_VALUE);
        }
    }
}
