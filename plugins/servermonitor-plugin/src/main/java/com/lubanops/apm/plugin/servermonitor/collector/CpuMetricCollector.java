/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.lubanops.apm.plugin.servermonitor.collector;


import com.lubanops.apm.plugin.servermonitor.command.Command;
import com.lubanops.apm.plugin.servermonitor.command.CommandExecutor;
import com.lubanops.apm.plugin.servermonitor.command.CpuCommand;
import com.lubanops.apm.plugin.servermonitor.entity.CpuMetric;

import static com.lubanops.apm.plugin.servermonitor.common.CalculateUtil.getPercentage;

/**
 * Linux CPU指标{@link CpuMetric}采集器，通过执行两次{@link CpuCommand}命令
 * 获取两次{@link CpuCommand.CpuStat}结果，来计算调用间隔时间内各种CPU时间占比。
 *
 * <p>每调用一次{@link #getCpuMetric()}方法会触发一次{@link CpuCommand}命令
 * 的执行，然后将本次执行的{@link CpuCommand.CpuStat}结果与上次执行的结果进行计
 * 算得到{@link CpuMetric}，并缓存本次执行结果用于下次计算</p>
 *
 * <p>重构泛PaaS：com.huawei.apm.plugin.collection.cpu.ServerCPUProvider
 * </p>
 */
public class CpuMetricCollector {

    private static final int SCALE = 0;

    private final CpuMetric emptyResult = new CpuMetric();

    private CpuCommand.CpuStat lastState;

    public CpuMetricCollector() {
        lastState = CommandExecutor.execute(Command.CPU);
    }

    /**
     * 获取cpu指标{@link CpuMetric}
     *
     * @return {@link CpuMetric}
     */
    public CpuMetric getCpuMetric() {
        final CpuCommand.CpuStat currentState = CommandExecutor.execute(Command.CPU);
        return currentState == null ? emptyResult : buildResult(currentState);
    }

    /**
     * user = user + nice 参照
     * <a href=https://github.com/i4oolish/nmon/blob/master/lnmon.c>nmon</a>
     *
     * @param currentState 本次采集的指标
     * @return CPU指标
     */
    private CpuMetric buildResult(CpuCommand.CpuStat currentState) {
        final long totalDiff = currentState.getTotal() - lastState.getTotal();
        CpuMetric cpuMetric;
        if (totalDiff > 0) {
            cpuMetric = CpuMetric.newBuilder()
                .withIdlePercentage(getPercentage(
                    currentState.getIdle() - lastState.getIdle(), totalDiff, SCALE).intValue())
                .withIoWaitPercentage(getPercentage(
                    currentState.getIoWait() - lastState.getIoWait(), totalDiff, SCALE).intValue())
                .withSysPercentage(getPercentage(
                    currentState.getSystem() - lastState.getSystem(), totalDiff, SCALE).intValue())
                .withUserPercentage(getPercentage(
                    (currentState.getUser() + currentState.getNice())
                        - (lastState.getUser() + lastState.getNice()), totalDiff, SCALE).intValue())
                .build();
        } else {
            // LOGGER.warn("Illegal state");
            cpuMetric = emptyResult;
        }
        lastState = currentState;
        return cpuMetric;
    }
}
