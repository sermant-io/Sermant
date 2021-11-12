/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.javamesh.sample.servermonitor.service;

import com.huawei.apm.core.service.CoreServiceManager;
import com.huawei.apm.core.service.PluginService;
import com.huawei.apm.core.lubanops.bootstrap.log.LogFactory;
import com.huawei.apm.core.service.send.GatewayClient;
import com.huawei.javamesh.sample.servermonitor.collector.CpuMetricCollector;
import com.huawei.javamesh.sample.servermonitor.collector.DiskMetricCollector;
import com.huawei.javamesh.sample.servermonitor.collector.MemoryMetricCollector;
import com.huawei.javamesh.sample.servermonitor.collector.NetworkMetricCollector;
import com.huawei.javamesh.sample.servermonitor.common.Consumer;
import com.huawei.javamesh.sample.servermonitor.common.Supplier;
import com.huawei.javamesh.sample.servermonitor.entity.ServerMetric;
import com.huawei.javamesh.sample.servermonitor.entity.ServerMetricCollection;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * CPU、磁盘、网络、磁盘指标采集服务
 *
 * 重构泛PaaS：com.huawei.apm.plugin.collection.ServerMonitorService
 */
public class ServerMonitorService implements PluginService {

    private static final Logger LOGGER = LogFactory.getLogger();

    private static final int GATEWAY_DATA_TYPE = 4;

    private CpuMetricCollector cpuMetricCollector;
    private DiskMetricCollector diskMetricCollector;
    private NetworkMetricCollector networkMetricCollector;
    private MemoryMetricCollector memoryMetricCollector;

    private CollectTask<ServerMetric> collectTask;

    private GatewayClient gatewayClient;

    @Override
    public void init() {
        // 此处用白名单比较合适，比如除了Windows外的其他非Linux也是不采集的
        boolean needCollect = !System.getProperty("os.name").contains("Windows");

        if (!needCollect) {
            LOGGER.info("The server monitor task does not need to start in current system.");
            return;
        }
        // Get from config
        final long collectInterval = 1;
        final long consumeInterval = 60;

        cpuMetricCollector = new CpuMetricCollector();
        diskMetricCollector = new DiskMetricCollector(collectInterval);
        networkMetricCollector = new NetworkMetricCollector(collectInterval);
        memoryMetricCollector = new MemoryMetricCollector();

        gatewayClient = CoreServiceManager.INSTANCE.getService(GatewayClient.class);

        collectTask = CollectTask.create(
            new Supplier<ServerMetric>() {
                @Override
                public ServerMetric get() {
                    return collect();
                }
            }, collectInterval,
            new Consumer<List<ServerMetric>>() {
                @Override
                public void accept(List<ServerMetric> serverMonitorMetrics) {
                    send(serverMonitorMetrics);
                }
            }, consumeInterval,
            TimeUnit.SECONDS);
        collectTask.start();
        LOGGER.info("Server monitor metric collect task started.");
    }

    @Override
    public void stop() {
        collectTask.stop();
    }

    private ServerMetric collect() {
        return ServerMetric.newBuilder()
            .setTime(System.currentTimeMillis())
            .setCpu(cpuMetricCollector.getCpuMetric())
            .addAllDisks(diskMetricCollector.getDiskMetrics())
            .setNetwork(networkMetricCollector.getNetworkMetric())
            .setMemory(memoryMetricCollector.getMemoryMetric())
            .build();
    }

    private void send(List<ServerMetric> metrics) {
        if (metrics == null || metrics.isEmpty()) {
            LOGGER.warning("No server monitor metric was collected.");
            return;
        }
        ServerMetricCollection collection = ServerMetricCollection.newBuilder()
            .setService("service") // 占位
            .setServiceInstance("service_instance") // 占位
            .addAllMetrics(metrics)
            .build();
        gatewayClient.send(collection.toByteArray(), GATEWAY_DATA_TYPE);
    }
}
