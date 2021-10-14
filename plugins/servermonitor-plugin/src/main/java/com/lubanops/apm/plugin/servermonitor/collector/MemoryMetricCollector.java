/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.lubanops.apm.plugin.servermonitor.collector;

import com.lubanops.apm.plugin.servermonitor.command.Command;
import com.lubanops.apm.plugin.servermonitor.command.CommandExecutor;
import com.lubanops.apm.plugin.servermonitor.command.MemoryCommand;
import com.lubanops.apm.plugin.servermonitor.entity.MemoryMetric;

/**
 * Linux memory指标{@link MemoryMetric}采集器
 *
 * <p>调用{@link #getMemoryMetric()}方法将执行{@link MemoryCommand}命令获取
 * {@link MemoryCommand.MemInfo}结果，然后使用此结果计算得到{@link MemoryMetric}。</p>
 *
 * <p>重构泛PaaS：com.huawei.apm.plugin.collection.memory.ServerMemoryProvider。</p>
 */
public class MemoryMetricCollector {

    private final MemoryMetric emptyResult = MemoryMetric.newBuilder().build();

    /**
     * 获取memory指标{@link MemoryMetric}
     *
     * @return {@link MemoryMetric}
     */
    public MemoryMetric getMemoryMetric() {
        final MemoryCommand.MemInfo memInfo = CommandExecutor.execute(Command.MEMORY);
        return memInfo == null ? emptyResult : buildResult(memInfo);
    }

    private MemoryMetric buildResult(MemoryCommand.MemInfo memInfo) {
        return MemoryMetric.newBuilder()
            .setMemoryTotal(memInfo.getMemoryTotal())
            .setMemoryUsed(memInfo.getMemoryTotal() - memInfo.getMemoryFree())
            .setBuffers(memInfo.getBuffers())
            .setCached(memInfo.getCached())
            .setSwapCached(memInfo.getSwapCached())
            .build();
    }
}
