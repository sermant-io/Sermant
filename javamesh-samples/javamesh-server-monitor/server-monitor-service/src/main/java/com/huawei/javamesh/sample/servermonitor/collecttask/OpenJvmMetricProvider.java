/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.javamesh.sample.servermonitor.collecttask;

import com.huawei.apm.core.lubanops.bootstrap.log.LogFactory;
import com.huawei.apm.core.plugin.config.PluginConfigManager;
import com.huawei.apm.core.service.ServiceManager;
import com.huawei.apm.core.service.send.GatewayClient;
import com.huawei.javamesh.sample.servermonitor.config.ServiceConfig;
import org.apache.skywalking.apm.agent.core.jvm.cpu.CPUProvider;
import org.apache.skywalking.apm.agent.core.jvm.gc.GCProvider;
import org.apache.skywalking.apm.agent.core.jvm.memory.MemoryProvider;
import org.apache.skywalking.apm.agent.core.jvm.memorypool.MemoryPoolProvider;
import org.apache.skywalking.apm.agent.core.jvm.thread.ThreadProvider;
import org.apache.skywalking.apm.network.language.agent.v3.JVMMetric;
import org.apache.skywalking.apm.network.language.agent.v3.JVMMetricCollection;

import java.util.List;
import java.util.logging.Logger;

/**
 * OpenJdk JVM Metric Provider
 */
public class OpenJvmMetricProvider implements MetricProvider<JVMMetric> {
    private static final Logger LOGGER = LogFactory.getLogger();
    private static final int GATEWAY_DATA_TYPE = 5;
    private static final OpenJvmMetricProvider INSTANCE = new OpenJvmMetricProvider();

    private final GatewayClient gatewayClient;
    private final String service;
    private final String serviceInstance;

    private OpenJvmMetricProvider() {
        ServiceConfig config = PluginConfigManager.getPluginConfig(ServiceConfig.class);
        service = config.getService();
        serviceInstance = config.getServiceInstance();
        gatewayClient = ServiceManager.getService(GatewayClient.class);
    }

    public static OpenJvmMetricProvider getInstance() {
        return INSTANCE;
    }

    @Override
    public JVMMetric collect() {
        final long currentTimeMillis = System.currentTimeMillis();
        return JVMMetric.newBuilder().setTime(currentTimeMillis)
            .setCpu(CPUProvider.INSTANCE.getCpuMetric())
            .addAllMemory(MemoryProvider.INSTANCE.getMemoryMetricList())
            .addAllMemoryPool(MemoryPoolProvider.INSTANCE.getMemoryPoolMetricsList())
            .addAllGc(GCProvider.INSTANCE.getGCList())
            .setThread(ThreadProvider.INSTANCE.getThreadMetrics())
            .build();
    }

    @Override
    public void consume(List<JVMMetric> metrics) {
        if (metrics == null || metrics.isEmpty()) {
            LOGGER.warning("No Oracle jvm metric was collected.");
            return;
        }
        JVMMetricCollection collection = JVMMetricCollection.newBuilder()
            .setService(service)
            .setServiceInstance(serviceInstance)
            .addAllMetrics(metrics)
            .build();
        gatewayClient.send(collection.toByteArray(), GATEWAY_DATA_TYPE);
    }
}
