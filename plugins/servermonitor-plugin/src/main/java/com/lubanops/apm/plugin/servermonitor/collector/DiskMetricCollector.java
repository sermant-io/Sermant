/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.lubanops.apm.plugin.servermonitor.collector;

import com.lubanops.apm.plugin.servermonitor.command.Command;
import com.lubanops.apm.plugin.servermonitor.command.CommandExecutor;
import com.lubanops.apm.plugin.servermonitor.command.DiskCommand;
import com.lubanops.apm.plugin.servermonitor.entity.DiskMetric;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static com.lubanops.apm.plugin.servermonitor.common.CalculateUtil.getPercentage;

/**
 * Linux disk指标{@link DiskMetric}采集器，通过执行两次{@link DiskCommand}命令
 * 获取两次{@link DiskCommand.DiskStats}结果，来计算每秒的磁盘读写字节数、和两次执
 * 行间隔时间段内IO时间所占的百分比。
 *
 * <p>每调用一次{@link #getDiskMetrics()}方法会触发一次{@link DiskCommand}命令
 * 的执行，然后将本次执行的{@link DiskCommand.DiskStats}结果与上次执行的结果进行计
 * 算得到{@link DiskMetric}，并缓存本次执行结果用于下次计算</p>
 *
 * <p>重构泛PaaS：com.huawei.apm.plugin.collection.disk.ServerDiskProvider
 * </p>
 */
public class DiskMetricCollector {

    /**
     * 扇区的字节大小
     */
    private static final int BYTES_PER_SECTOR = 512;

    private static final int IO_SPENT_SCALE = 2;

    /**
     * 采集周期毫秒，用于计算IO占比
     */
    private final long collectCycleMills;

    /**
     * 计算每秒读写字节数的因子
     */
    private final long multiPlyFactor;

    /**
     * key: diskName
     * value: DiskStats
     */
    private final Map<String, DiskCommand.DiskStats> lastDiskStats = new HashMap<String, DiskCommand.DiskStats>();

    /**
     * 空结果缓存
     * key: diskName
     * value: DiskMetric
     */
    private final Map<String, DiskMetric> emptyResults = new HashMap<String, DiskMetric>();

    public DiskMetricCollector(long collectCycle) {
        this.collectCycleMills = TimeUnit.MILLISECONDS.convert(collectCycle, TimeUnit.SECONDS);
        this.multiPlyFactor = BYTES_PER_SECTOR / collectCycle;
        List<DiskCommand.DiskStats> diskStats = CommandExecutor.execute(Command.DISK);
        if (diskStats == null) {
            return;
        }
        for (DiskCommand.DiskStats diskStat : diskStats) {
            String deviceName = diskStat.getDeviceName();
            lastDiskStats.put(deviceName, diskStat);
            emptyResults.put(deviceName, new DiskMetric(deviceName));
        }
    }

    /**
     * 获取disk指标{@link DiskMetric}
     *
     * @return {@link DiskMetric}
     */
    public List<DiskMetric> getDiskMetrics() {
        final List<DiskCommand.DiskStats> currentDiskStats = CommandExecutor.execute(Command.DISK);
        if (currentDiskStats != null && !currentDiskStats.isEmpty()) {
            return buildResults(currentDiskStats);
        } else {
            return emptyResults();
        }
    }

    private List<DiskMetric> buildResults(List<DiskCommand.DiskStats> currentDiskStats) {
        final List<DiskMetric> diskMetrics = new LinkedList<DiskMetric>();
        for (final DiskCommand.DiskStats currentDiskStat : currentDiskStats) {
            String deviceName = currentDiskStat.getDeviceName();
            DiskCommand.DiskStats lastDiskStat = lastDiskStats.remove(deviceName);

            long readBytesPerSec;
            long writeBytesPerSec;
            double ioSpentPercentage;

            if (lastDiskStat == null) {
                // 如果上次采集的数据中不包含当前disk，则为新添加的disk
                readBytesPerSec = currentDiskStat.getSectorsRead() * multiPlyFactor;
                writeBytesPerSec = currentDiskStat.getSectorsWritten() * multiPlyFactor;
                ioSpentPercentage = getPercentage(
                    currentDiskStat.getIoSpentMillis(), collectCycleMills, IO_SPENT_SCALE).doubleValue();
                // 缓存新disk的空结果
                emptyResults.put(deviceName, new DiskMetric(deviceName));
            } else {
                readBytesPerSec = (currentDiskStat.getSectorsRead() - lastDiskStat.getSectorsRead()) * multiPlyFactor;
                writeBytesPerSec = (currentDiskStat.getSectorsWritten() - lastDiskStat.getSectorsWritten()) * multiPlyFactor;
                ioSpentPercentage = getPercentage(
                    currentDiskStat.getIoSpentMillis() - lastDiskStat.getIoSpentMillis(),
                    collectCycleMills, IO_SPENT_SCALE).doubleValue();
            }
            diskMetrics.add(new DiskMetric(deviceName, readBytesPerSec, writeBytesPerSec, ioSpentPercentage));
        }

        // 如果上次采集的disk在本次采集中不存在，则表示disk被移除
        Set<String> removedDeviceNames = lastDiskStats.keySet();
        for (String deviceName : removedDeviceNames) {
            diskMetrics.add(emptyResults.get(deviceName));
        }

        // 更新disk状态
        for (DiskCommand.DiskStats diskStat : currentDiskStats) {
            lastDiskStats.put(diskStat.getDeviceName(), diskStat);
        }
        return diskMetrics;
    }

    private List<DiskMetric> emptyResults() {
        return Collections.unmodifiableList(new ArrayList<DiskMetric>(emptyResults.values()));
    }
}
