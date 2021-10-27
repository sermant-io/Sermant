/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.javamesh.sample.servermonitor.service;

import com.huawei.apm.bootstrap.boot.CoreServiceManager;
import com.huawei.apm.bootstrap.boot.PluginService;
import com.huawei.apm.bootstrap.service.send.UnifiedGatewayClient;
import com.huawei.javamesh.sample.servermonitor.common.Consumer;
import com.huawei.javamesh.sample.servermonitor.common.Supplier;
import org.apache.skywalking.apm.agent.core.jvm.cpu.CPUProvider;
import org.apache.skywalking.apm.agent.core.jvm.gc.GCProvider;
import org.apache.skywalking.apm.agent.core.jvm.memory.MemoryProvider;
import org.apache.skywalking.apm.agent.core.jvm.memorypool.MemoryPoolProvider;
import org.apache.skywalking.apm.agent.core.jvm.thread.ThreadProvider;
import org.apache.skywalking.apm.network.language.agent.v3.JVMMetric;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Oracle JVM采集服务
 */
public class OracleJvmCollectService implements PluginService {

    private final static int GATEWAY_DATA_TYPE = 5;

    private CollectTask<JVMMetric> collectTask;

    private UnifiedGatewayClient gatewayClient;

    @Override
    public void init() {
        // Get from config
        final long collectInterval = 1;
        final long consumeInterval = 60;

        gatewayClient = CoreServiceManager.INSTANCE.getService(UnifiedGatewayClient.class);

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
            // LOG
            return;
        }
        for (JVMMetric metric : metrics) {
            gatewayClient.send(metric.toByteArray(), GATEWAY_DATA_TYPE);
        }
    }
}
