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

import com.huawei.monitor.common.MemoryType;
import com.huawei.monitor.common.MetricEnum;
import com.huawei.monitor.common.MetricFamilyBuild;
import com.huawei.monitor.util.CollectionUtil;

import com.huaweicloud.sermant.core.plugin.service.PluginService;

import com.sun.management.OperatingSystemMXBean;

import io.prometheus.client.GaugeMetricFamily;

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryUsage;
import java.lang.management.ThreadMXBean;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * JVM性能指标采集器
 *
 * @author zhp
 * @version 1.0.0
 * @since 2022-08-02
 */
public class JvmCollectorService extends SwitchService implements PluginService {
    private static final List<String> NEW_GEN_NAME_LIST = Arrays.asList("PS Scavenge", "ParNew", "Copy", "G1 Young "
            + "Generation", "ZGC Cycles");

    private static final int PERCENT = 100;

    @Override
    public List<MetricFamilySamples> collect() {
        List<MetricFamilySamples> metricFamilySamplesList = new ArrayList<>();
        OperatingSystemMXBean operatingSystemMxBean =
                (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
        GaugeMetricFamily gaugeMetricFamily = MetricFamilyBuild.buildGaugeMetric(MetricEnum.CPU_USED,
                operatingSystemMxBean.getProcessCpuLoad() * PERCENT);
        metricFamilySamplesList.add(gaugeMetricFamily);
        MemoryMXBean memoryMxBean = ManagementFactory.getMemoryMXBean();
        fillMetric(metricFamilySamplesList, MemoryType.HEAP_MEMORY.getType(), memoryMxBean.getHeapMemoryUsage());
        fillMetric(metricFamilySamplesList, MemoryType.NON_HEAP_MEMORY.getType(), memoryMxBean.getNonHeapMemoryUsage());
        fillGcMetric(metricFamilySamplesList, ManagementFactory.getGarbageCollectorMXBeans());
        fillThreadMetric(metricFamilySamplesList, ManagementFactory.getThreadMXBean());
        fillMemoryMetric(metricFamilySamplesList, ManagementFactory.getMemoryPoolMXBeans());
        GaugeMetricFamily startTimeMetric = MetricFamilyBuild.buildGaugeMetric(MetricEnum.START_TIME,
                System.currentTimeMillis() - ManagementFactory.getRuntimeMXBean().getStartTime());
        metricFamilySamplesList.add(startTimeMetric);
        return metricFamilySamplesList;
    }

    /**
     * 填充GC指标信息
     *
     * @param metricList 指标收集集合
     * @param gcMxBeans  GC信息
     */
    private void fillGcMetric(List<MetricFamilySamples> metricList, List<GarbageCollectorMXBean> gcMxBeans) {
        if (CollectionUtil.isEmpty(gcMxBeans)) {
            return;
        }
        gcMxBeans.forEach(gcMxBean -> {
            if (NEW_GEN_NAME_LIST.contains(gcMxBean.getName())) {
                metricList.add(MetricFamilyBuild.buildGaugeMetric(MetricEnum.NEW_GEN_COUNT,
                        gcMxBean.getCollectionCount()));
                metricList.add(MetricFamilyBuild.buildGaugeMetric(MetricEnum.NEW_GEN_SPEND,
                        gcMxBean.getCollectionTime()));
            } else {
                metricList.add(MetricFamilyBuild.buildGaugeMetric(MetricEnum.OLD_GEN_COUNT,
                        gcMxBean.getCollectionCount()));
                metricList.add(MetricFamilyBuild.buildGaugeMetric(MetricEnum.OLD_GEN_SPEND,
                        gcMxBean.getCollectionTime()));
            }
        });
    }

    /**
     * 填充内存指标信息
     *
     * @param metricList        指标信息集合
     * @param memoryPoolMxBeans 内存指标信息
     */
    private void fillMemoryMetric(List<MetricFamilySamples> metricList, List<MemoryPoolMXBean> memoryPoolMxBeans) {
        if (CollectionUtil.isEmpty(memoryPoolMxBeans)) {
            return;
        }
        memoryPoolMxBeans.forEach(memoryPoolMxBean -> {
            if (memoryPoolMxBean.getUsage() != null) {
                fillMetric(metricList, memoryPoolMxBean.getName(), memoryPoolMxBean.getUsage());
            }
        });
    }

    /**
     * 收集线程指标信息
     *
     * @param metricList   性能指标信息
     * @param threadMxBean 线程信息
     */
    private void fillThreadMetric(List<MetricFamilySamples> metricList, ThreadMXBean threadMxBean) {
        metricList.add(MetricFamilyBuild.buildGaugeMetric(MetricEnum.THREAD_LIVE, threadMxBean.getThreadCount()));
        metricList.add(MetricFamilyBuild.buildGaugeMetric(MetricEnum.THREAD_DAEMON,
                threadMxBean.getDaemonThreadCount()));
        metricList.add(MetricFamilyBuild.buildGaugeMetric(MetricEnum.THREAD_PEAK, threadMxBean.getPeakThreadCount()));
    }

    /**
     * 填充内存指标
     *
     * @param metricFamilySamplesList 指标集合
     * @param type                    类型
     * @param memoryUsage             指标信息
     */
    private void fillMetric(List<MetricFamilySamples> metricFamilySamplesList, String type, MemoryUsage memoryUsage) {
        Optional<MemoryType> optional = MemoryType.getEnumByType(type);
        if (!optional.isPresent()) {
            return;
        }
        MemoryType memoryType = optional.get();
        metricFamilySamplesList.add(MetricFamilyBuild.buildGaugeMetric(memoryType.getInitEnum(),
                memoryUsage.getInit()));
        metricFamilySamplesList.add(MetricFamilyBuild.buildGaugeMetric(memoryType.getMaxEnum(), memoryUsage.getMax()));
        metricFamilySamplesList.add(MetricFamilyBuild.buildGaugeMetric(memoryType.getUsedEnum(),
                memoryUsage.getUsed()));
        metricFamilySamplesList.add(MetricFamilyBuild.buildGaugeMetric(memoryType.getCommittedEnum(),
                memoryUsage.getCommitted()));
    }
}
