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
 * 命令获取两次{@link NetworkCommand.NetDev}结果，来计算每秒的网络读写字节和包数量。
 *
 * <p>每调用一次{@link #getNetworkMetric()}方法会触发一次{@link NetworkCommand}命令
 * 的执行，然后将本次执行的{@link NetworkCommand.NetDev}结果与上次执行的结果进行计算得到
 * {@link NetworkMetric}，并缓存本次执行结果用于下次计算</p>
 *
 * <p>重构泛PaaS：com.huawei.apm.plugin.collection.network.ServerNetWorkProvider
 * </p>
 */
public class NetworkMetricCollector {

    private final NetworkMetric emptyResult = new NetworkMetric();

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
     * 获取服务器网络信息
     *
     * @return 返回ServerNetWork对象
     */
    public NetworkMetric getNetworkMetric() {
        final NetworkCommand.NetDev currentNetDev = CommandExecutor.execute(Command.NETWORK);
        return currentNetDev == null ? emptyResult : buildResult(currentNetDev);
    }

    private NetworkMetric buildResult(NetworkCommand.NetDev currentNetDev) {
        NetworkMetric networkMetric = NetworkMetric.newBuilder()
            .withReadBytesPerSec((currentNetDev.getReceiveBytes() - lastNetDev.getReceiveBytes()) / collectCycle)
            .withReadPackagesPerSec((currentNetDev.getReceivePackets() - lastNetDev.getReceivePackets()) / collectCycle)
            .withWriteBytesPerSec((currentNetDev.getTransmitBytes() - lastNetDev.getTransmitBytes()) / collectCycle)
            .withWritePackagesPerSec((currentNetDev.getTransmitPackets() - lastNetDev.getTransmitPackets()) / collectCycle)
            .build();
        lastNetDev = currentNetDev;
        return networkMetric;
    }
}
