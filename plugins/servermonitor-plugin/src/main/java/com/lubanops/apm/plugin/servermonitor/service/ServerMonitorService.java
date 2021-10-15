/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.lubanops.apm.plugin.servermonitor.service;

import com.huawei.apm.bootstrap.boot.PluginService;
import com.lubanops.apm.plugin.servermonitor.collector.CpuMetricCollector;
import com.lubanops.apm.plugin.servermonitor.collector.DiskMetricCollector;
import com.lubanops.apm.plugin.servermonitor.collector.IbmJvmMetricCollector;
import com.lubanops.apm.plugin.servermonitor.collector.MemoryMetricCollector;
import com.lubanops.apm.plugin.servermonitor.collector.NetworkMetricCollector;
import com.lubanops.apm.plugin.servermonitor.common.Consumer;
import com.lubanops.apm.plugin.servermonitor.common.Supplier;
import com.lubanops.apm.plugin.servermonitor.entity.ServerMonitorMetric;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * CPU、磁盘 IO、网络IO、物理磁盘利用率、IBM jvm数据采集服务
 *
 * 重构泛PaaS：com.huawei.apm.plugin.collection.ServerMonitorService
 */
public class ServerMonitorService implements PluginService {

    private boolean collectServerMetric;

    private boolean collectIbmJvmMetric;

    private CpuMetricCollector cpuMetricCollector;
    private DiskMetricCollector diskMetricCollector;
    private NetworkMetricCollector networkMetricCollector;
    private MemoryMetricCollector memoryMetricCollector;

    private IbmJvmMetricCollector ibmJvmMetricCollector;

    private CollectTask<ServerMonitorMetric> collectTask;

    //@Override
    public void init() {
        // TODO 此处用白名单比较合适，比如除了Windows外的其他非Linux也是不采集的
        collectServerMetric = !System.getProperty("os.name").contains("Windows");
        collectIbmJvmMetric = System.getProperty("java.vm.vendor").contains("IBM");
        if (!collectServerMetric && !collectIbmJvmMetric) {
            // LOGGER.info("The server monitor task does not need to start in current system and jvm arch.")
            return;
        }

        // TODO Get from config
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
                .setNetWork(networkMetricCollector.getNetworkMetric())
                .setMemory(memoryMetricCollector.getMemoryMetric());
        }
        // IBM JDK JVM指标不应该和以上Linux指标混到一块，但目前后台逻辑已经这么处理了，暂时保留
        if (collectIbmJvmMetric) {
            builder.addAllIbmJvmMetrics(ibmJvmMetricCollector.getIbmJvmMetrics());
        }
        return builder.build();
    }

    private void send(List<ServerMonitorMetric> metrics) {
        //TODO 发送
        if (metrics.isEmpty()) {
            // LOG
            return;
        }
        // TODO remove test code
        for (ServerMonitorMetric metric : metrics) {
            System.out.println(metric);
        }
        System.out.println();
    }
}
