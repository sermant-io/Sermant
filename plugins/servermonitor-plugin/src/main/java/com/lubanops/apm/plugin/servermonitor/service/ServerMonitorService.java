package com.lubanops.apm.plugin.servermonitor.service;

import com.huawei.apm.bootstrap.boot.PluginService;
import com.lubanops.apm.plugin.servermonitor.collector.CpuMetricCollector;
import com.lubanops.apm.plugin.servermonitor.collector.DiskMetricCollector;
import com.lubanops.apm.plugin.servermonitor.collector.MemoryMetricCollector;
import com.lubanops.apm.plugin.servermonitor.collector.NetworkMetricCollector;
import com.lubanops.apm.plugin.servermonitor.common.Consumer;
import com.lubanops.apm.plugin.servermonitor.common.Supplier;
import com.lubanops.apm.plugin.servermonitor.entity.ServerMonitorMetric;
import com.lubanops.apm.plugin.servermonitor.jvm.MemoryPoolProvider;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * CPU、磁盘 IO、网络IO、物理磁盘利用率、IBM jvm数据采集服务
 *
 * 重构泛PaaS：com.huawei.apm.plugin.collection.ServerMonitorService
 */
public class ServerMonitorService implements PluginService {

    private boolean shouldCollectLinux;

    private boolean isIbmJvm;

    private DiskMetricCollector diskMetricCollector;
    private CpuMetricCollector cpuMetricCollector;
    private MemoryMetricCollector memoryMetricCollector;
    private NetworkMetricCollector networkMetricCollector;

    private CollectTask<ServerMonitorMetric> collectTask;

    @Override
    public void init() {
        // TODO 此处用白名单比较合适，比如除了Windows外的其他非Linux也是不采集的
        shouldCollectLinux = !System.getProperty("os.name").contains("Windows");
        isIbmJvm = System.getProperty("java.vm.vendor").contains("IBM");
        if (!shouldCollectLinux && !isIbmJvm) {
            // LOGGER.info("The server monitor task does not need to start in current system and jvm arch.")
            return;
        }

        // TODO Get from config
        final long collectInterval = 1;
        final long consumeInterval = 60;

        if (shouldCollectLinux) {
            diskMetricCollector = new DiskMetricCollector(collectInterval);
            cpuMetricCollector = new CpuMetricCollector();
            memoryMetricCollector = new MemoryMetricCollector();
            networkMetricCollector = new NetworkMetricCollector(collectInterval);
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
        if (shouldCollectLinux) {
            builder.setCpu(cpuMetricCollector.getCpuMetric())
                .setDisks(diskMetricCollector.getDiskMetrics())
                .setNetwork(networkMetricCollector.getNetworkMetric())
                .setMemory(memoryMetricCollector.getMemoryMetric());
        }
        // IBM JDK JVM指标不应该和以上Linux指标混到一块，但目前后台逻辑已经这么处理了，暂时保留
        if (isIbmJvm) {
            builder.setIbmMemoryPools(MemoryPoolProvider.INSTANCE.getMemoryPoolMetricsList());
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
