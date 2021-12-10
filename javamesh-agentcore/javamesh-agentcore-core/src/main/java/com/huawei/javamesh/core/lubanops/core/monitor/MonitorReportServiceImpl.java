/*
 * Copyright (C) 2021-2021 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huawei.javamesh.core.lubanops.core.monitor;

import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.alibaba.fastjson.JSONObject;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.huawei.javamesh.core.lubanops.bootstrap.api.HarvestListener;
import com.huawei.javamesh.core.lubanops.bootstrap.commons.LubanApmConstants;
import com.huawei.javamesh.core.lubanops.bootstrap.config.AgentConfigManager;
import com.huawei.javamesh.core.lubanops.bootstrap.exception.ApmRuntimeException;
import com.huawei.javamesh.core.lubanops.bootstrap.log.LogFactory;
import com.huawei.javamesh.core.lubanops.bootstrap.plugin.apm.APMCollector;
import com.huawei.javamesh.core.lubanops.bootstrap.utils.AgentUtils;
import com.huawei.javamesh.core.lubanops.core.api.AgentService;
import com.huawei.javamesh.core.lubanops.core.common.ConnectionException;
import com.huawei.javamesh.core.lubanops.core.container.Priority;
import com.huawei.javamesh.core.lubanops.core.executor.standalone.ServiceThread;
import com.huawei.javamesh.core.lubanops.core.transfer.InvokerService;
import com.huawei.javamesh.core.lubanops.core.utils.ReportDataBuilder;

import com.huawei.javamesh.core.lubanops.integration.access.MessageIdGenerator;
import com.huawei.javamesh.core.lubanops.integration.access.inbound.MonitorDataBody;
import com.huawei.javamesh.core.lubanops.integration.access.inbound.MonitorDataRequest;
import com.huawei.javamesh.core.lubanops.integration.transport.ClientManager;
import com.huawei.javamesh.core.lubanops.integration.transport.netty.client.NettyClient;
import com.huawei.javamesh.core.lubanops.integration.transport.netty.pojo.Message;

/**
 * Monitor Queue Service.
 * <p/>
 * this service run as a standalone thread for sending monitor data.
 * @author
 */
@Singleton
public class MonitorReportServiceImpl extends ServiceThread implements MonitorReportService, AgentService {
    public static final int DEFAULT_QUEUE_SIZE = 128;

    private final static Logger LOGGER = LogFactory.getLogger();

    final NettyClient nettyClient = ClientManager.getNettyClientFactory().getNettyClient(
            AgentConfigManager.getNettyServerIp(),
            Integer.parseInt(AgentConfigManager.getNettyServerPort()));

    /**
     * default queue size
     */
    private int dataQueueSize = DEFAULT_QUEUE_SIZE;

    /**
     * collector data cache
     */
    public Queue<MonitorDataBody> collectorDataQueue = new ArrayBlockingQueue<MonitorDataBody>(dataQueueSize);

    /**
     * exception flag
     */
    private boolean hasException = Boolean.FALSE;

    /**
     * self monitor data
     */
    private volatile MonitorDataRequest innerRequest = null;

    /**
     * transfer service
     */
    @Inject
    private InvokerService invokerService;

    private HarvestListener harvestListener;

    private volatile boolean closed = false;

    @Override
    public String getServiceName() {
        return MonitorReportServiceImpl.class.getSimpleName();
    }

    // ~~ public methods

    @Override
    public void run() {
        LOGGER.info("[MONITOR REPORTER]reporter start.");
        while (!this.isStopped() && doReport()) {
            // do nothing
        }
        LOGGER.info("[MONITOR REPORTER]reporter stopped.");
    }

    @Override
    public int getPriority() {
        return Priority.AGENT_INTERNAL_SERVICE;
    }

    @Override
    public void init() throws ApmRuntimeException {
        harvestListener = new HarvestListener<APMCollector>() {
            @Override
            public void onHarvest(APMCollector collector, long time) {
                collector.traceQueueSize(collectorDataQueue.size(), AgentUtils.getObjectSize(collectorDataQueue));
            }
        };
        APMCollector.INSTANCE.listenHarvest(harvestListener);
        this.start();
    }

    @Override
    public void dispose() throws ApmRuntimeException {
        if (closed) {
            return;
        }
        closed = true;
        int shutdownTimeout = LubanApmConstants.DEFAULT_SERVICE_SHUTDOWN_TIMEOUT;
        if (shutdownTimeout > 0) {
            long start = System.currentTimeMillis();
            while (collectorDataQueue.isEmpty() && System.currentTimeMillis() - start < shutdownTimeout) {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    LOGGER.log(Level.SEVERE, e.getMessage(), e);
                }
            }
        }
        collectorDataQueue.clear();
        shutdown(true);
    }

    /**
     * offer collect data
     * @param body
     * @return
     */
    @Override
    public boolean offer(MonitorDataBody body) {
        boolean success = collectorDataQueue.offer(body);
        if (!success) {
            String bodyStr = body.toString();
            APMCollector.onDiscard(LubanApmConstants.MONITOR_DATA_TYPE, bodyStr.getBytes().length);
            LOGGER.warning("data queue is full,data discarded:" + bodyStr);
        } else {
            APMCollector.onStart(LubanApmConstants.MONITOR_DATA_TYPE, collectorDataQueue.size());
        }
        return success;
    }

    /**
     * send internal monitor data
     */
    @Override
    public void reportInnerData(MonitorDataBody body) {
        try {
            if (this.innerRequest == null) {
                innerRequest = new MonitorDataRequest();
                innerRequest.setHeader(ReportDataBuilder.buildMonitorDataHeader());
            }
            innerRequest.setBody(body);
            if (invokerService.isSendEnable()) {
                this.invokerService.sendDataReport(innerRequest);
            }
            this.innerRequest = null;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE,
                    "failed to send data,error message:" + e.getMessage() + ",type:" + e.getClass().getName(), e);
            LOGGER.log(Level.SEVERE, "data:" + innerRequest);
        }
    }

    @Override
    public void onWaitEnd() {

    }
    // ~~ inner methods

    /**
     * internal send method
     * @return
     */
    private boolean doReport() {
        MonitorDataRequest request = new MonitorDataRequest();
        MonitorDataBody body = null;
        long startTime = 0;
        long length = 0;
        try {
            request.setHeader(ReportDataBuilder.buildMonitorDataHeader());
            request.setMessageId(MessageIdGenerator.generateMessageId());
            request.getHeader().setNeedResponse(false);
            body = collectorDataQueue.poll();
            if (body == null) {
                super.waitForRunning(50);
                return true;
            }
            request.setBody(body);
            startTime = System.currentTimeMillis();
            String bodyStr = request.getBodyString();
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.log(Level.FINE, String.format("[debug mode]sending message:[%s]", request));
            }
            length = bodyStr.getBytes().length;
            if (length > 1000000) {
                LOGGER.log(Level.SEVERE, "data过大:" + bodyStr);
                APMCollector.onThrowable(LubanApmConstants.MONITOR_DATA_TYPE, length,
                        new ApmRuntimeException("上报数据超过1M"));
            }
            sendData(request.getBody());
        } catch (Exception e) {
            if (!hasException) {
                LOGGER.log(Level.SEVERE, this.getServiceName() + " has exception.", e);
            } else if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.log(Level.FINE, this.getServiceName() + " has exception.", e);
            }
            APMCollector.onThrowable(LubanApmConstants.MONITOR_DATA_TYPE, length, e);
            this.hasException = true;
        } finally {
            if (body != null) {
                APMCollector.onFinally(LubanApmConstants.MONITOR_DATA_TYPE, System.currentTimeMillis() - startTime);
            }
        }
        return true;
    }

    private void sendData(MonitorDataRequest request, String bodyStr, long length)
            throws ConnectionException, IOException {
        if (invokerService.isSendEnable()) {
            invokerService.sendDataReport(request);
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.log(Level.FINE,
                        String.format("[debug mode]sending message success:[%s]", request.getMessageId()));
            }
        } else {
            APMCollector.onDiscard(LubanApmConstants.MONITOR_DATA_TYPE, bodyStr.getBytes().length);
        }
        APMCollector.onSuccess(LubanApmConstants.MONITOR_DATA_TYPE, length);
    }


    private void sendData(MonitorDataBody monitorDataBody) {
        if (invokerService.isSendEnable()) {
            try {
                nettyClient.sendData(JSONObject.toJSONBytes(monitorDataBody), Message.ServiceData.DataType.AGENT_MONITOR);
            } catch (Exception e) {
                LOGGER.severe("[sendData] error:" + e.getMessage());
            }
        }
    }
}
