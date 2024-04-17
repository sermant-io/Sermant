/*
 * Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huawei.monitor.service.collector;

import com.huawei.monitor.command.Command;
import com.huawei.monitor.command.CommandExecutor;
import com.huawei.monitor.command.CpuCommand;
import com.huawei.monitor.command.CpuInfoCommand;
import com.huawei.monitor.command.DiskCommand;
import com.huawei.monitor.command.MemoryCommand;
import com.huawei.monitor.command.NetworkCommand;
import com.huawei.monitor.common.MetricEnum;
import com.huawei.monitor.common.MetricFamilyBuild;

import com.huaweicloud.sermant.core.common.LoggerFactory;
import com.huaweicloud.sermant.core.plugin.service.PluginService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * Server performance metric collector
 *
 * @author zhp
 * @version 1.0.0
 * @since 2022-08-02
 */
public class ServerCollectorService extends SwitchService implements PluginService {
    private static final Logger LOGGER = LoggerFactory.getLogger();

    private static final BigDecimal HUNDRED = new BigDecimal("100");

    private static final long INTERVAL = 1L;

    private static final int SCALE = 0;

    private static final int BYTES_PER_SECTOR = 512;

    private static final int IO_SPENT_SCALE = 2;

    private static final int MILLIS = 1000;

    @Override
    public List<MetricFamilySamples> collect() {
        List<MetricFamilySamples> metricFamilySamplesList = new ArrayList<>();
        collectMemoryMetric(metricFamilySamplesList);
        Optional<CpuCommand.CpuStat> cpuStatOptional = CommandExecutor.execute(Command.CPU);
        Optional<NetworkCommand.NetDev> netDevOptional = CommandExecutor.execute(Command.NETWORK);
        Optional<List<DiskCommand.DiskStats>> diskStatsListOptional = CommandExecutor.execute(Command.DISK);
        collectCpuMetric(metricFamilySamplesList, cpuStatOptional);
        collectNetWorkMetric(metricFamilySamplesList, netDevOptional);
        collectDiskMetric(metricFamilySamplesList, diskStatsListOptional);
        Optional<CpuInfoCommand.CpuInfoStat> cpuInfoStatOptional = CommandExecutor.execute(Command.CPU_INFO);
        cpuInfoStatOptional.ifPresent(cpuInfoStat -> metricFamilySamplesList.add(MetricFamilyBuild.buildGaugeMetric(
                MetricEnum.CPU_CORES, cpuInfoStat.getTotalCores())));
        return metricFamilySamplesList;
    }

    private static void sleep() {
        try {
            Thread.sleep(INTERVAL * MILLIS);
        } catch (InterruptedException e) {
            LOGGER.warning("thread sleep is interrupted");
        }
    }

    private void collectDiskMetric(List<MetricFamilySamples> metricList,
            Optional<List<DiskCommand.DiskStats>> statsListOptional) {
        Optional<List<DiskCommand.DiskStats>> currentStatsListOptional = CommandExecutor.execute(Command.DISK);
        if (!statsListOptional.isPresent() || !currentStatsListOptional.isPresent()) {
            return;
        }
        List<DiskCommand.DiskStats> statsList = statsListOptional.get();
        List<DiskCommand.DiskStats> currentStatsList = currentStatsListOptional.get();
        Map<String, DiskCommand.DiskStats> map = new HashMap<>();
        statsList.forEach(stats -> map.put(stats.getDeviceName(), stats));
        double totalSectorsRead = 0d;
        double totalSectorsWritten = 0d;
        double totalIoSpent = 0d;
        for (DiskCommand.DiskStats currentStats : currentStatsList) {
            DiskCommand.DiskStats stats = map.get(currentStats.getDeviceName());
            if (stats != null) {
                totalSectorsRead += currentStats.getSectorsRead() - stats.getSectorsRead();
                totalSectorsWritten += currentStats.getSectorsWritten() - stats.getSectorsWritten();
                totalIoSpent += currentStats.getIoSpentMillis() - stats.getIoSpentMillis();
            } else {
                totalSectorsRead += currentStats.getSectorsRead();
                totalSectorsWritten += currentStats.getSectorsWritten();
                totalIoSpent += currentStats.getIoSpentMillis();
            }
        }
        metricList.add(MetricFamilyBuild.buildGaugeMetric(MetricEnum.DISK_READ_BYTE_SPEED,
                totalSectorsRead * BYTES_PER_SECTOR / INTERVAL));
        metricList.add(MetricFamilyBuild.buildGaugeMetric(MetricEnum.DISK_WRITE_BYTE_SPEED,
                totalSectorsWritten * BYTES_PER_SECTOR / INTERVAL));
        long divideNum = INTERVAL * MILLIS * currentStatsList.size();
        metricList.add(MetricFamilyBuild.buildGaugeMetric(MetricEnum.DISK_IO_SPENT, getPercentage(totalIoSpent,
                divideNum, IO_SPENT_SCALE).doubleValue()));
    }

    private void collectNetWorkMetric(List<MetricFamilySamples> metricFamilySamplesList,
            Optional<NetworkCommand.NetDev> netDevOptional) {
        Optional<NetworkCommand.NetDev> currentNetDevOptional = CommandExecutor.execute(Command.NETWORK);
        if (!netDevOptional.isPresent() || !currentNetDevOptional.isPresent()) {
            return;
        }
        NetworkCommand.NetDev currentNetDev = currentNetDevOptional.get();
        NetworkCommand.NetDev netDev = netDevOptional.get();
        long readBytesSpeed = (currentNetDev.getReceiveBytes() - netDev.getReceiveBytes()) / INTERVAL;
        long writeBytesSpeed = (currentNetDev.getTransmitBytes() - netDev.getTransmitBytes()) / INTERVAL;
        long readPackageSpeed = (currentNetDev.getReceivePackets() - netDev.getReceivePackets()) / INTERVAL;
        long writePackageSpeed = (currentNetDev.getTransmitPackets() - netDev.getTransmitPackets()) / INTERVAL;
        metricFamilySamplesList.add(MetricFamilyBuild.buildGaugeMetric(MetricEnum.NETWORK_READ_BYTE_SPEED,
                readBytesSpeed));
        metricFamilySamplesList.add(MetricFamilyBuild.buildGaugeMetric(MetricEnum.NETWORK_WRITE_BYTE_SPEED,
                writeBytesSpeed));
        metricFamilySamplesList.add(MetricFamilyBuild.buildGaugeMetric(MetricEnum.NETWORK_READ_PACKAGE_SPEED,
                readPackageSpeed));
        metricFamilySamplesList.add(MetricFamilyBuild.buildGaugeMetric(MetricEnum.NETWORK_WRITE_PACKAGE_SPEED,
                writePackageSpeed));
    }

    private void collectMemoryMetric(List<MetricFamilySamples> metricFamilySamplesList) {
        Optional<MemoryCommand.MemInfo> memInfoOptional = CommandExecutor.execute(Command.MEMORY);
        if (!memInfoOptional.isPresent()) {
            return;
        }
        MemoryCommand.MemInfo memInfo = memInfoOptional.get();
        metricFamilySamplesList.add(MetricFamilyBuild.buildGaugeMetric(MetricEnum.MEMORY_USED,
                memInfo.getMemoryTotal() - memInfo.getMemoryFree() - memInfo.getBuffers() - memInfo.getCached()));
        metricFamilySamplesList.add(MetricFamilyBuild.buildGaugeMetric(MetricEnum.MEMORY_TOTAL,
                memInfo.getMemoryTotal()));
        metricFamilySamplesList.add(MetricFamilyBuild.buildGaugeMetric(MetricEnum.MEMORY_BUFFER, memInfo.getBuffers()));
        metricFamilySamplesList.add(MetricFamilyBuild.buildGaugeMetric(MetricEnum.MEMORY_SWAP,
                memInfo.getSwapCached()));
        metricFamilySamplesList.add(MetricFamilyBuild.buildGaugeMetric(MetricEnum.MEMORY_CACHE, memInfo.getCached()));
    }

    /**
     * Collect CPU metric information and take the difference between the two metrics
     *
     * @param metricFamilySamplesList metric information collection
     * @param cpuStatOptional cpu information
     */
    private void collectCpuMetric(List<MetricFamilySamples> metricFamilySamplesList,
            Optional<CpuCommand.CpuStat> cpuStatOptional) {
        sleep();
        Optional<CpuCommand.CpuStat> currentCpuStatOptional = CommandExecutor.execute(Command.CPU);
        if (!cpuStatOptional.isPresent() || !currentCpuStatOptional.isPresent()) {
            return;
        }
        CpuCommand.CpuStat currentCpuStat = currentCpuStatOptional.get();
        CpuCommand.CpuStat cpuStat = cpuStatOptional.get();
        long idleAdd = currentCpuStat.getIdle() - cpuStat.getIdle();
        long totalAdd = currentCpuStat.getTotal() - cpuStat.getTotal();
        long waitAdd = currentCpuStat.getIoWait() - cpuStat.getIoWait();
        long sysAdd = currentCpuStat.getSystem() - cpuStat.getSystem();
        long userAdd = currentCpuStat.getUser() - cpuStat.getUser() + currentCpuStat.getNice() - cpuStat.getNice();
        metricFamilySamplesList.add(MetricFamilyBuild.buildGaugeMetric(MetricEnum.CPU_USER, getPercentage(userAdd,
                totalAdd, SCALE).doubleValue()));
        metricFamilySamplesList.add(MetricFamilyBuild.buildGaugeMetric(MetricEnum.CPU_IDLE, getPercentage(idleAdd,
                totalAdd, SCALE).doubleValue()));
        metricFamilySamplesList.add(MetricFamilyBuild.buildGaugeMetric(MetricEnum.CPU_WAIT, getPercentage(waitAdd,
                totalAdd, SCALE).doubleValue()));
        metricFamilySamplesList.add(MetricFamilyBuild.buildGaugeMetric(MetricEnum.CPU_SYS, getPercentage(sysAdd,
                totalAdd, SCALE).doubleValue()));
    }

    private static BigDecimal getPercentage(double numerate, long denominator, int scale) {
        return BigDecimal.valueOf(numerate).multiply(HUNDRED).divide(BigDecimal.valueOf(denominator), scale,
                RoundingMode.HALF_UP);
    }
}
