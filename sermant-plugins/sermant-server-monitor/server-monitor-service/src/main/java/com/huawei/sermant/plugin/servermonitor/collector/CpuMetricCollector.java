/*
 * Copyright (C) 2021-2021 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huawei.sermant.plugin.servermonitor.collector;

import com.huawei.sermant.plugin.servermonitor.command.Command;
import com.huawei.sermant.plugin.servermonitor.command.CommandExecutor;
import com.huawei.sermant.plugin.servermonitor.command.CpuCommand;
import com.huawei.sermant.plugin.servermonitor.entity.CpuMetric;

import static com.huawei.sermant.plugin.monitor.common.utils.CommonUtil.getPercentage;

/**
 * Linux CPU指标{@link CpuMetric}采集器，通过执行两次{@link CpuCommand}命令
 * 获取两次{@link CpuCommand.CpuStat}结果，来计算调用间隔时间内各种CPU时间占总
 * CPU时间的百分比。
 *
 * <p>每调用一次{@link #getCpuMetric()}方法会触发一次{@link CpuCommand}命令
 * 的执行，然后将本次执行的{@link CpuCommand.CpuStat}结果与上次执行的结果进行计
 * 算得到{@link CpuMetric}，并缓存本次执行结果用于下次计算。第一次采集会得到空闲
 * 率{@link CpuMetric#getIdlePercentage()}为100，其他值为0的初始值。</p>
 *
 * <p>重构泛PaaS：com.huawei.sermant.plugin.collection.cpu.ServerCPUProvider。
 * </p>
 */
public class CpuMetricCollector {

    private static final int SCALE = 0;

    private final CpuMetric emptyResult = CpuMetric.newBuilder().setIdlePercentage(100).build();

    private CpuCommand.CpuStat lastState;

    /**
     * 获取cpu指标{@link CpuMetric}
     *
     * @return {@link CpuMetric}
     */
    public CpuMetric getCpuMetric() {
        final CpuCommand.CpuStat currentState = CommandExecutor.execute(Command.CPU);
        return currentState == null ? emptyResult : buildResult(currentState);
    }

    private CpuMetric buildResult(CpuCommand.CpuStat currentState) {
        CpuMetric cpuMetric;
        if (lastState == null || lastState.getTotal() >= currentState.getTotal()) {
            cpuMetric = emptyResult;
        } else {
            cpuMetric = CpuMetric.newBuilder()
                .setIdlePercentage(calcIdlePercentage(currentState))
                .setIoWaitPercentage(calcIoWaitPercentage(currentState))
                .setSysPercentage(calcSysPercentage(currentState))
                .setUserPercentage(calcUserPercentage(currentState))
                .build();
        }
        lastState = currentState;
        return cpuMetric;
    }

    private int calcIdlePercentage(CpuCommand.CpuStat currentState) {
        return getPercentage(
            currentState.getIdle() - lastState.getIdle(),
            currentState.getTotal() - lastState.getTotal(), SCALE).intValue();
    }

    private int calcIoWaitPercentage(CpuCommand.CpuStat currentState) {
        return getPercentage(
            currentState.getIoWait() - lastState.getIoWait(),
            currentState.getTotal() - lastState.getTotal(), SCALE).intValue();
    }

    private int calcSysPercentage(CpuCommand.CpuStat currentState) {
        return getPercentage(
            currentState.getSystem() - lastState.getSystem(),
            currentState.getTotal() - lastState.getTotal(), SCALE).intValue();
    }

    /**
     * user = user + nice 参照
     * <a href=https://github.com/i4oolish/nmon/blob/master/lnmon.c>nmon</a>
     */
    private int calcUserPercentage(CpuCommand.CpuStat currentState) {
        return getPercentage(
            (currentState.getUser() + currentState.getNice()) - (lastState.getUser() + lastState.getNice()),
            currentState.getTotal() - lastState.getTotal(), SCALE).intValue();
    }
}
