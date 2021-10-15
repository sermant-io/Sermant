/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.lubanops.apm.plugin.servermonitor.collector;

import com.lubanops.apm.plugin.servermonitor.command.Command;
import com.lubanops.apm.plugin.servermonitor.command.CommandExecutor;
import com.lubanops.apm.plugin.servermonitor.command.NetworkCommand;
import com.lubanops.apm.plugin.servermonitor.entity.NetworkMetric;

/**
 * Linux network指标{@link NetworkMetric}采集器，通过执行两次{@link NetworkCommand}
 * 命令获取两次{@link NetworkCommand.NetDev}结果，来计算计算两次执行时间间隔内每秒的网络
 * 读写字节和包数量。
 *
 * <p>每调用一次{@link #getNetworkMetric()}方法会触发一次{@link NetworkCommand}命令
 * 的执行，然后将本次执行的{@link NetworkCommand.NetDev}结果与上次执行的结果进行计算得到
 * {@link NetworkMetric}，并缓存本次执行结果用于下次计算。</p>
 *
 * <p>重构泛PaaS：com.huawei.apm.plugin.collection.network.ServerNetWorkProvider。
 * </p>
 */
public class NetworkMetricCollector {

    private final NetworkMetric emptyResult = NetworkMetric.newBuilder().build();

    /**
     * 采集周期，单位：秒
     */
    private final long collectCycle;

    private NetworkCommand.NetDev lastNetDev;

    public NetworkMetricCollector(long collectCycle) {
        this.collectCycle = collectCycle;
        lastNetDev = CommandExecutor.execute(Command.NETWORK);
    }

    /**
     * 获取Network指标{@link NetworkMetric}
     *
     * @return {@link NetworkMetric}
     */
    public NetworkMetric getNetworkMetric() {
        final NetworkCommand.NetDev currentNetDev = CommandExecutor.execute(Command.NETWORK);
        return currentNetDev == null ? emptyResult : buildResult(currentNetDev);
    }

    private NetworkMetric buildResult(NetworkCommand.NetDev currentNetDev) {
        NetworkMetric networkMetric;
        if (lastNetDev == null) {
            networkMetric = emptyResult;
        } else {
            networkMetric = NetworkMetric.newBuilder()
                .setReadBytesPerSec(calcReadBytesPerSec(currentNetDev))
                .setReadPackagesPerSec(calcReadPackagesPerSec(currentNetDev))
                .setWriteBytesPerSec(calcWriteBytesPerSec(currentNetDev))
                .setWritePackagesPerSec(calcWritePackagesPerSec(currentNetDev))
                .build();
        }
        lastNetDev = currentNetDev;
        return networkMetric;
    }

    private long calcReadBytesPerSec(NetworkCommand.NetDev currentNetDev) {
        return (currentNetDev.getReceiveBytes() - lastNetDev.getReceiveBytes()) / collectCycle;
    }

    private long calcReadPackagesPerSec(NetworkCommand.NetDev currentNetDev) {
        return (currentNetDev.getReceivePackets() - lastNetDev.getReceivePackets()) / collectCycle;
    }

    private long calcWriteBytesPerSec(NetworkCommand.NetDev currentNetDev) {
        return (currentNetDev.getTransmitBytes() - lastNetDev.getTransmitBytes()) / collectCycle;
    }

    private long calcWritePackagesPerSec(NetworkCommand.NetDev currentNetDev) {
        return (currentNetDev.getTransmitPackets() - lastNetDev.getTransmitPackets()) / collectCycle;
    }
}
