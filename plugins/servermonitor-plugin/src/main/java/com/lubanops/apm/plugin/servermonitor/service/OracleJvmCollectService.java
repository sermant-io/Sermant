/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.lubanops.apm.plugin.servermonitor.service;

import com.huawei.apm.bootstrap.boot.PluginService;
import com.lubanops.apm.plugin.servermonitor.common.Consumer;
import com.lubanops.apm.plugin.servermonitor.common.Supplier;
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

    private CollectTask<JVMMetric> collectTask;

    @Override
    public void init() {
        // TODO Get from config
        final long collectInterval = 1;
        final long consumeInterval = 60;

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
        //TODO 发送
        if (metrics.isEmpty()) {
            // LOG
            return;
        }
        // TODO remove test code
        for (JVMMetric metric : metrics) {
            System.out.println(metric);
        }
        System.out.println();
    }
}
