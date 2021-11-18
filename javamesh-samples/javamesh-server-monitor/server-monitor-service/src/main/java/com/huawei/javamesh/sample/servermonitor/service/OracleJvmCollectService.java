/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.javamesh.sample.servermonitor.service;

import com.huawei.apm.core.service.ServiceManager;
import com.huawei.apm.core.plugin.service.PluginService;
import com.huawei.apm.core.lubanops.bootstrap.log.LogFactory;
import com.huawei.apm.core.service.send.GatewayClient;
import com.huawei.javamesh.sample.servermonitor.common.Consumer;
import com.huawei.javamesh.sample.servermonitor.common.Supplier;
import org.apache.skywalking.apm.agent.core.jvm.cpu.CPUProvider;
import org.apache.skywalking.apm.agent.core.jvm.gc.GCProvider;
import org.apache.skywalking.apm.agent.core.jvm.memory.MemoryProvider;
import org.apache.skywalking.apm.agent.core.jvm.memorypool.MemoryPoolProvider;
import org.apache.skywalking.apm.agent.core.jvm.thread.ThreadProvider;
import org.apache.skywalking.apm.network.language.agent.v3.JVMMetric;
import org.apache.skywalking.apm.network.language.agent.v3.JVMMetricCollection;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * Oracle JVM采集服务
 */
public class OracleJvmCollectService implements PluginService {

    private static final Logger LOGGER = LogFactory.getLogger();

    private final static int GATEWAY_DATA_TYPE = 5;

    private CollectTask<JVMMetric> collectTask;

    private GatewayClient gatewayClient;

    @Override
    public void start() {
        // Get from config
        final long collectInterval = 1;
        final long consumeInterval = 60;

        gatewayClient = ServiceManager.getService(GatewayClient.class);

        collectTask = CollectTask.create(
            new Supplier<JVMMetric>() {
                @Override
                public JVMMetric get() {
                    return collect();
                }
            }, collectInterval,
            new Consumer<List<JVMMetric>>() {
                @Override
                public void accept(List<JVMMetric> serverMonitoringMetrics) {
                    send(serverMonitoringMetrics);
                }
            }, consumeInterval,
            TimeUnit.SECONDS);
        collectTask.start();
        LOGGER.info("Oracle jvm metric collect task started.");
    }

    @Override
    public void stop() {
        collectTask.stop();
    }

    private JVMMetric collect() {
        final long currentTimeMillis = System.currentTimeMillis();
        return JVMMetric.newBuilder().setTime(currentTimeMillis)
            .setCpu(CPUProvider.INSTANCE.getCpuMetric())
            .addAllMemory(MemoryProvider.INSTANCE.getMemoryMetricList())
            .addAllMemoryPool(MemoryPoolProvider.INSTANCE.getMemoryPoolMetricsList())
            .addAllGc(GCProvider.INSTANCE.getGCList())
            .setThread(ThreadProvider.INSTANCE.getThreadMetrics())
            .build();
    }

    private void send(List<JVMMetric> metrics) {
        if (metrics == null || metrics.isEmpty()) {
            LOGGER.warning("No Oracle jvm metric was collected.");
            return;
        }

        JVMMetricCollection collection = JVMMetricCollection.newBuilder()
            .setService("service") // 占位
            .setServiceInstance("service_instance") // 占位
            .addAllMetrics(metrics)
            .build();
        gatewayClient.send(collection.toByteArray(), GATEWAY_DATA_TYPE);
    }
}
