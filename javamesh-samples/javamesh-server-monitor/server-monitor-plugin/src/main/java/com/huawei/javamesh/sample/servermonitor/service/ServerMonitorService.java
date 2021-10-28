/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.javamesh.sample.servermonitor.service;

import com.huawei.apm.bootstrap.boot.CoreServiceManager;
import com.huawei.apm.bootstrap.boot.PluginService;
import com.huawei.apm.bootstrap.lubanops.log.LogFactory;
import com.huawei.apm.bootstrap.service.send.GatewayClient;
import com.huawei.javamesh.sample.servermonitor.collector.CpuMetricCollector;
import com.huawei.javamesh.sample.servermonitor.collector.DiskMetricCollector;
import com.huawei.javamesh.sample.servermonitor.collector.IbmJvmMetricCollector;
import com.huawei.javamesh.sample.servermonitor.collector.MemoryMetricCollector;
import com.huawei.javamesh.sample.servermonitor.collector.NetworkMetricCollector;
import com.huawei.javamesh.sample.servermonitor.common.Consumer;
import com.huawei.javamesh.sample.servermonitor.common.Supplier;
import com.huawei.javamesh.sample.servermonitor.entity.ServerMonitorMetric;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * CPU、磁盘 IO、网络IO、物理磁盘利用率、IBM jvm数据采集服务
 *
 * 重构泛PaaS：com.huawei.apm.plugin.collection.ServerMonitorService
 */
public class ServerMonitorService implements PluginService {

    private static final Logger LOGGER = LogFactory.getLogger();

    private final static int GATEWAY_DATA_TYPE = 4;

    private boolean collectServerMetric;

    private boolean collectIbmJvmMetric;

    private CpuMetricCollector cpuMetricCollector;
    private DiskMetricCollector diskMetricCollector;
    private NetworkMetricCollector networkMetricCollector;
    private MemoryMetricCollector memoryMetricCollector;

    private IbmJvmMetricCollector ibmJvmMetricCollector;

    private CollectTask<ServerMonitorMetric> collectTask;

    private GatewayClient gatewayClient;

    @Override
    public void init() {
        // 此处用白名单比较合适，比如除了Windows外的其他非Linux也是不采集的
        collectServerMetric = !System.getProperty("os.name").contains("Windows");
        collectIbmJvmMetric = System.getProperty("java.vm.vendor").contains("IBM");
        if (!collectServerMetric && !collectIbmJvmMetric) {
            LOGGER.info("The server monitor task does not need to start in current system and jvm arch.");
            return;
        }

        // Get from config
        final long collectInterval = 1;
        final long consumeInterval = 60;

        if (collectServerMetric) {
            cpuMetricCollector = new CpuMetricCollector();
            diskMetricCollector = new DiskMetricCollector(collectInterval);
            networkMetricCollector = new NetworkMetricCollector(collectInterval);
            memoryMetricCollector = new MemoryMetricCollector();
        }
        if (collectIbmJvmMetric) {
            ibmJvmMetricCollector = new IbmJvmMetricCollector();
        }

        gatewayClient = CoreServiceManager.INSTANCE.getService(GatewayClient.class);

        collectTask = CollectTask.create(
            new Supplier<ServerMonitorMetric>() {
                @Override
                public ServerMonitorMetric get() {
                    return collect();
                }
            }, collectInterval,
            new Consumer<List<ServerMonitorMetric>>() {
                @Override
                public void accept(List<ServerMonitorMetric> serverMonitorMetrics) {
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

    private ServerMonitorMetric collect() {
        ServerMonitorMetric.Builder builder = ServerMonitorMetric.newBuilder()
            .setTime(System.currentTimeMillis());
        if (collectServerMetric) {
            builder.setCpu(cpuMetricCollector.getCpuMetric())
                .addAllDisks(diskMetricCollector.getDiskMetrics())
                .setNetwork(networkMetricCollector.getNetworkMetric())
                .setMemory(memoryMetricCollector.getMemoryMetric());
        }
        // IBM JDK JVM指标不应该和以上Linux指标混到一块，但目前后台逻辑已经这么处理了，暂时保留
        if (collectIbmJvmMetric) {
            builder.addAllIbmJvmMetrics(ibmJvmMetricCollector.getIbmJvmMetrics());
        }
        return builder.build();
    }

    private void send(List<ServerMonitorMetric> metrics) {
        if (metrics == null || metrics.isEmpty()) {
            LOGGER.warning("No server monitor metric was collected.");
            return;
        }
        for (ServerMonitorMetric metric : metrics) {
            gatewayClient.send(metric.toByteArray(), GATEWAY_DATA_TYPE);
        }
    }
}
