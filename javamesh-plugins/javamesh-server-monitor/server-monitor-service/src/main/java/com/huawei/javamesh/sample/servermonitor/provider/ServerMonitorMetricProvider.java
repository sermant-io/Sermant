/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.javamesh.sample.servermonitor.provider;

import com.huawei.javamesh.core.common.LoggerFactory;
import com.huawei.javamesh.core.plugin.config.PluginConfigManager;
import com.huawei.javamesh.core.service.ServiceManager;
import com.huawei.javamesh.core.service.send.GatewayClient;
import com.huawei.javamesh.sample.monitor.common.collect.MetricProvider;
import com.huawei.javamesh.sample.monitor.common.config.ServiceConfig;
import com.huawei.javamesh.sample.servermonitor.collector.CpuMetricCollector;
import com.huawei.javamesh.sample.servermonitor.collector.DiskMetricCollector;
import com.huawei.javamesh.sample.servermonitor.collector.MemoryMetricCollector;
import com.huawei.javamesh.sample.servermonitor.collector.NetworkMetricCollector;
import com.huawei.javamesh.sample.servermonitor.entity.ServerMetric;
import com.huawei.javamesh.sample.servermonitor.entity.ServerMetricCollection;

import java.util.List;
import java.util.logging.Logger;

/**
 * CPU、磁盘、网络、磁盘 Metric Provider
 */
public class ServerMonitorMetricProvider implements MetricProvider<ServerMetric> {
    private static final Logger LOGGER = LoggerFactory.getLogger();
    private static final int GATEWAY_DATA_TYPE = 4;

    private final CpuMetricCollector cpuMetricCollector;
    private final DiskMetricCollector diskMetricCollector;
    private final NetworkMetricCollector networkMetricCollector;
    private final MemoryMetricCollector memoryMetricCollector;

    private final GatewayClient gatewayClient;
    private final String service;
    private  final String serviceInstance;

    public ServerMonitorMetricProvider(long collectInterval) {
        ServiceConfig config = PluginConfigManager.getPluginConfig(ServiceConfig.class);
        service = config.getService();
        serviceInstance = config.getServiceInstance();
        gatewayClient = ServiceManager.getService(GatewayClient.class);
        cpuMetricCollector = new CpuMetricCollector();
        diskMetricCollector = new DiskMetricCollector(collectInterval);
        networkMetricCollector = new NetworkMetricCollector(collectInterval);
        memoryMetricCollector = new MemoryMetricCollector();
    }

    @Override
    public ServerMetric collect() {
        return ServerMetric.newBuilder()
            .setTime(System.currentTimeMillis())
            .setCpu(cpuMetricCollector.getCpuMetric())
            .addAllDisks(diskMetricCollector.getDiskMetrics())
            .setNetwork(networkMetricCollector.getNetworkMetric())
            .setMemory(memoryMetricCollector.getMemoryMetric())
            .build();
    }

    @Override
    public void consume(List<ServerMetric> metrics) {
        if (metrics == null || metrics.isEmpty()) {
            LOGGER.warning("No server monitor metric was collected.");
            return;
        }
        ServerMetricCollection collection = ServerMetricCollection.newBuilder()
            .setService(service)
            .setServiceInstance(serviceInstance)
            .addAllMetrics(metrics)
            .build();
        gatewayClient.send(collection.toByteArray(), GATEWAY_DATA_TYPE);
    }
}
