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
import com.huawei.sermant.plugin.servermonitor.command.DiskCommand;
import com.huawei.sermant.plugin.servermonitor.entity.DiskMetric;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.huawei.sermant.plugin.monitor.common.utils.CommonUtil.getPercentage;

/**
 * Linux disk指标{@link DiskMetric}采集器，通过执行两次{@link DiskCommand}命令
 * 获取两次{@link DiskCommand.DiskStats}结果，来计算两次执行时间间隔内每秒的磁盘读
 * 写字节数、和该时间段内IO消耗时间所占的百分比。
 *
 * <p>每调用一次{@link #getDiskMetrics()}方法会触发一次{@link DiskCommand}命令
 * 的执行，然后将本次执行的{@link DiskCommand.DiskStats}结果与上次执行的结果进行计
 * 算得到{@link DiskMetric}，并缓存本次执行结果用于下次计算。第一次调用会得到各项数值
 * 都为0的{@link DiskMetric}。</p>
 *
 * <p>重构泛PaaS：com.huawei.sermant.plugin.collection.disk.ServerDiskProvider。
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
     * 指标零值缓存，新设备或存量设备第一次的指标使用零值
     * key: diskName
     * value: DiskMetric
     */
    private final Map<String, DiskMetric> emptyResults = new HashMap<String, DiskMetric>();

    public DiskMetricCollector(long collectCycle) {
        if (collectCycle <= 0) {
            throw new IllegalArgumentException("Collect cycle must be positive.");
        }
        this.collectCycleMills = TimeUnit.MILLISECONDS.convert(collectCycle, TimeUnit.SECONDS);
        this.multiPlyFactor = BYTES_PER_SECTOR / collectCycle;
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
            final String deviceName = currentDiskStat.getDeviceName();
            // 根据设备名从last disk中检索并移除disk状态（判断是否存在）
            final DiskCommand.DiskStats lastDiskStat = lastDiskStats.remove(deviceName);
            DiskMetric diskMetric;
            if (lastDiskStat == null) {
                // 如果上次采集的数据中不包含当前disk，则其为新添加的disk，缓存新disk的“零值”
                diskMetric = DiskMetric.newBuilder()
                    .setDeviceName(deviceName)
                    .build();
                emptyResults.put(deviceName, diskMetric);
            } else {
                diskMetric = DiskMetric.newBuilder()
                    .setDeviceName(deviceName)
                    .setReadBytesPerSec(calcReadBytesPerSec(currentDiskStat, lastDiskStat))
                    .setWriteBytesPerSec(calcWriteBytesPerSec(currentDiskStat, lastDiskStat))
                    .setIoSpentPercentage(calcIoSpentPercentage(currentDiskStat, lastDiskStat))
                    .build();
            }
            diskMetrics.add(diskMetric);
        }

        // last disk中剩下的disk表示，之前存在过，但在本次采集中已不存在的disk，即被移除的disk
        for (String deviceName : lastDiskStats.keySet()) {
            diskMetrics.add(emptyResults.get(deviceName));
        }

        // 更新last disk状态
        for (DiskCommand.DiskStats diskStat : currentDiskStats) {
            lastDiskStats.put(diskStat.getDeviceName(), diskStat);
        }
        return diskMetrics;
    }

    private double calcIoSpentPercentage(DiskCommand.DiskStats currentDiskStat, DiskCommand.DiskStats lastDiskStat) {
        return getPercentage(
            currentDiskStat.getIoSpentMillis() - lastDiskStat.getIoSpentMillis(),
            collectCycleMills, IO_SPENT_SCALE).doubleValue();
    }

    private long calcWriteBytesPerSec(DiskCommand.DiskStats currentDiskStat, DiskCommand.DiskStats lastDiskStat) {
        return (currentDiskStat.getSectorsWritten() - lastDiskStat.getSectorsWritten()) * multiPlyFactor;
    }

    private long calcReadBytesPerSec(DiskCommand.DiskStats currentDiskStat, DiskCommand.DiskStats lastDiskStat) {
        return (currentDiskStat.getSectorsRead() - lastDiskStat.getSectorsRead()) * multiPlyFactor;
    }

    private List<DiskMetric> emptyResults() {
        return Collections.unmodifiableList(new ArrayList<DiskMetric>(emptyResults.values()));
    }
}
