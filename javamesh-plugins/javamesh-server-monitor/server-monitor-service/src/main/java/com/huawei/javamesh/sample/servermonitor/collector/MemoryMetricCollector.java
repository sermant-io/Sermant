/*
 * Copyright (C) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.huawei.javamesh.sample.servermonitor.collector;

import com.huawei.javamesh.sample.servermonitor.command.Command;
import com.huawei.javamesh.sample.servermonitor.command.CommandExecutor;
import com.huawei.javamesh.sample.servermonitor.command.MemoryCommand;
import com.huawei.javamesh.sample.servermonitor.entity.MemoryMetric;

/**
 * Linux memory指标{@link MemoryMetric}采集器
 *
 * <p>调用{@link #getMemoryMetric()}方法将执行{@link MemoryCommand}命令获取
 * {@link MemoryCommand.MemInfo}结果，然后使用此结果计算得到{@link MemoryMetric}。</p>
 *
 * <p>重构泛PaaS：com.huawei.javamesh.plugin.collection.memory.ServerMemoryProvider。</p>
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
