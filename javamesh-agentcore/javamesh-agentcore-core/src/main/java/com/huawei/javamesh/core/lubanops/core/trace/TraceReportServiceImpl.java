/*
 * Copyright (C) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved
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

package com.huawei.javamesh.core.lubanops.core.trace;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.alibaba.fastjson.JSONObject;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.huawei.javamesh.core.lubanops.bootstrap.api.APIService;
import com.huawei.javamesh.core.lubanops.bootstrap.api.HarvestListener;
import com.huawei.javamesh.core.lubanops.bootstrap.commons.LubanApmConstants;
import com.huawei.javamesh.core.lubanops.bootstrap.config.AgentConfigManager;
import com.huawei.javamesh.core.lubanops.bootstrap.exception.ApmRuntimeException;
import com.huawei.javamesh.core.lubanops.bootstrap.log.LogFactory;
import com.huawei.javamesh.core.lubanops.bootstrap.plugin.apm.APMCollector;
import com.huawei.javamesh.core.lubanops.bootstrap.trace.SpanEvent;
import com.huawei.javamesh.core.lubanops.bootstrap.trace.TraceReportService;
import com.huawei.javamesh.core.lubanops.bootstrap.utils.AgentUtils;
import com.huawei.javamesh.core.lubanops.core.api.AgentService;
import com.huawei.javamesh.core.lubanops.core.common.ConnectionException;
import com.huawei.javamesh.core.lubanops.core.executor.standalone.ServiceThread;
import com.huawei.javamesh.core.lubanops.core.transfer.InvokerService;
import com.huawei.javamesh.core.lubanops.core.utils.ReportDataBuilder;
import com.huawei.javamesh.core.lubanops.integration.access.MessageIdGenerator;
import com.huawei.javamesh.core.lubanops.integration.access.inbound.EventDataBody;
import com.huawei.javamesh.core.lubanops.integration.access.inbound.EventDataRequest;
import com.huawei.javamesh.core.lubanops.integration.transport.ClientManager;
import com.huawei.javamesh.core.lubanops.integration.transport.netty.client.NettyClient;
import com.huawei.javamesh.core.lubanops.integration.transport.netty.pojo.Message;

/**
 * TraceQueueService 发送数据
 * @author
 */
@Singleton
public class TraceReportServiceImpl implements TraceReportService, AgentService {

    private final static int MAX_SPAN_EVENT_COUNT = 512;

    private final static String SPAN_EVENT_DATA_TYPE = "SpanEventData";

    private final static Queue<SpanEvent> SPAN_EVENT_DATA_QUEUE = new ArrayBlockingQueue<SpanEvent>(
            MAX_SPAN_EVENT_COUNT);

    private final static Logger LOGGER = LogFactory.getLogger();

    final NettyClient nettyClient = ClientManager.getNettyClientFactory().getNettyClient(
            AgentConfigManager.getNettyServerIp(),
            Integer.parseInt(AgentConfigManager.getNettyServerPort()));

    @Inject
    private InvokerService invokerService;

    private HarvestListener<APMCollector> harvestListener;

    private List<SpanEventSendThread> threadList = new ArrayList<SpanEventSendThread>();

    private boolean closed = false;

    /**
     * exception flag
     */
    private boolean hasException = Boolean.FALSE;

    public TraceReportServiceImpl() {
        int threadCount = AgentConfigManager.getEventThreadCount();
        for (int i = 0; i < threadCount; i++) {
            SpanEventSendThread spanEventSendThread = new SpanEventSendThread();
            spanEventSendThread.start();
            threadList.add(spanEventSendThread);
        }
        harvestListener = new HarvestListener<APMCollector>() {
            @Override
            public void onHarvest(APMCollector collector, long time) {
                collector.monitorQueueSize(SPAN_EVENT_DATA_QUEUE.size(),
                        AgentUtils.getObjectSize(SPAN_EVENT_DATA_QUEUE));
            }
        };
        APMCollector.INSTANCE.listenHarvest(harvestListener);
    }

    @Override
    public void init() throws ApmRuntimeException {
    }

    @Override
    public void dispose() throws ApmRuntimeException {
        if (closed) {
            return;
        }
        closed = true;
        int timeout = LubanApmConstants.DEFAULT_SERVICE_SHUTDOWN_TIMEOUT;
        if (timeout > 0) {
            long start = System.currentTimeMillis();
            while (SPAN_EVENT_DATA_QUEUE.isEmpty() && System.currentTimeMillis() - start < timeout) {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    LOGGER.log(Level.SEVERE, e.getMessage(), e);
                }
            }
        }
        SPAN_EVENT_DATA_QUEUE.clear();
        for (SpanEventSendThread spanEventSendThread : threadList) {
            spanEventSendThread.shutdown(true);
        }
        threadList.clear();
    }

    @Override
    public int getPriority() {
        return AGENT_INTERNAL_SERVICE;
    }

    @Override
    public void offerEvent(SpanEvent spanEvent) {
        if (spanEvent == null) {
            return;
        }
        boolean success = SPAN_EVENT_DATA_QUEUE.offer(spanEvent);
        if (!success) {
            String json = APIService.getJsonApi().toJSONString(spanEvent);
            int length = json.getBytes().length;
            APMCollector.onDiscard(SPAN_EVENT_DATA_TYPE, length);
            LogFactory.getLogger().warning("spanevent data queue is full,data discarded:" + spanEvent.toString());
        } else {
            APMCollector.onStart(SPAN_EVENT_DATA_TYPE, SPAN_EVENT_DATA_QUEUE.size());
        }
    }

    public class SpanEventSendThread extends ServiceThread {
        private EventDataRequest request;

        private long length;

        @Override
        public void run() {
            LOGGER.info("[TRACE REPORTER]reporter start.");
            while (!this.isStopped() && report()) {

            }
            LOGGER.info("[TRACE REPORTER]reporter stop.");
        }

        public boolean report() {
            SpanEvent spanEvent = null;
            long endTime = 0;
            long startTime = 0;
            String logClassName = null;
            String logMethodName = null;
            try {
                spanEvent = SPAN_EVENT_DATA_QUEUE.poll();
                if (spanEvent == null) {
                    super.waitForRunning(50);
                    return true;
                }
                startTime = System.currentTimeMillis();
                request = new EventDataRequest();
                request.setMessageId(MessageIdGenerator.generateMessageId());
                request.setHeader(ReportDataBuilder.buildEventDataHeader());
                request.getHeader().setNeedResponse(false);
                EventDataBody eventDataBody = ReportDataBuilder.buildEventDataBody(spanEvent);
                request.setBody(eventDataBody);
                length = request.getBodyBytes().length;
                if (length > 1000000) {
                    LOGGER.log(Level.SEVERE, "data过大:" + request.getBodyString().substring(0, 1000));
                    Map<String, String> bodyMap = new HashMap<String, String>();
                    bodyMap.put("msg", "数据超过1M已丢弃");
                    eventDataBody.setTags(bodyMap);
                    length = eventDataBody.toString().getBytes().length;
                }
                logClassName = request.getBody().getClassName();
                logMethodName = request.getBody().getMethod();
                sendSpanEvent(request.getBody());
            } catch (Throwable e) {
                Level level = Level.SEVERE;
                if (hasException) {
                    level = Level.FINE;
                }
                LOGGER.log(level,
                        "failed to send data,error message:" + e.getMessage() + ",type:" + e.getClass().getName(),
                        e);
                LOGGER.log(Level.SEVERE, "data:" + logClassName + "." + logMethodName);
                APMCollector.onThrowable(LubanApmConstants.SPAN_EVENT_DATA_TYPE, length, e);
                hasException = true;
            } finally {
                if (startTime > 0) {
                    endTime = System.currentTimeMillis();
                    APMCollector.onFinally(LubanApmConstants.SPAN_EVENT_DATA_TYPE, endTime - startTime);
                }
            }
            return true;
        }

        public void sendSpanEvent() throws ConnectionException, IOException {
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.log(Level.FINE, String.format("[debug mode]sending message:[%s]", request.getBodyString()));
            }
            if (invokerService.isSendEnable()) {
                invokerService.sendDataReport(request);
                if (LOGGER.isLoggable(Level.FINE)) {
                    LOGGER.log(Level.FINE,
                        String.format("[debug mode]sending message success:[%s]", request.getMessageId()));
                }
            } else {
                APMCollector.onDiscard(SPAN_EVENT_DATA_TYPE, length);
            }
            APMCollector.onSuccess(LubanApmConstants.SPAN_EVENT_DATA_TYPE, length);

        }

        public void sendSpanEvent(EventDataBody eventDataBody) {
            if (invokerService.isSendEnable()) {
                try {
                    nettyClient.sendData(JSONObject.toJSONBytes(eventDataBody), Message.ServiceData.DataType.AGENT_SPAN_EVENT);
                } catch (Exception e) {
                    LOGGER.severe("[sendSpanEvent] error:" + e.getMessage());
                }
            }
        }

        @Override
        public String getServiceName() {
            return TraceReportServiceImpl.class.getSimpleName();
        }

        @Override
        protected void onWaitEnd() {

        }
    }
}
