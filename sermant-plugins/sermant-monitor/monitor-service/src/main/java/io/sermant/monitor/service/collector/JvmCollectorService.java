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

package io.sermant.monitor.service.collector;

import com.sun.management.OperatingSystemMXBean;

import io.prometheus.client.GaugeMetricFamily;
import io.sermant.core.plugin.service.PluginService;
import io.sermant.monitor.common.MemoryType;
import io.sermant.monitor.common.MetricEnum;
import io.sermant.monitor.common.MetricFamilyBuild;
import io.sermant.monitor.util.CollectionUtil;

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryUsage;
import java.lang.management.ThreadMXBean;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * jvm performance metric collector
 *
 * @author zhp
 * @version 1.0.0
 * @since 2022-08-02
 */
public class JvmCollectorService extends SwitchService implements PluginService {
    private static final Set<String> NEW_GEN_NAME_SET = new HashSet<>(Arrays.asList("PS Scavenge", "ParNew", "Copy",
            "G1 Young Generation", "ZGC Cycles", "Shenandoah Cycles"));

    /**
     * The name of the generationless Epsilon GC in JDK11
     */
    private static final String EPSILON_GEN_NAME = "Epsilon Heap";

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
     * fill in gc metric information
     *
     * @param metricList metric collection list
     * @param gcMxBeans GC information
     */
    private void fillGcMetric(List<MetricFamilySamples> metricList, List<GarbageCollectorMXBean> gcMxBeans) {
        if (CollectionUtil.isEmpty(gcMxBeans)) {
            return;
        }
        gcMxBeans.forEach(gcMxBean -> {
            long genCount = gcMxBean.getCollectionCount();
            long genSpend = gcMxBean.getCollectionTime();
            if (EPSILON_GEN_NAME.equals(gcMxBean.getName())) {
                metricList.add(MetricFamilyBuild.buildGaugeMetric(MetricEnum.NEW_GEN_COUNT,
                        genCount));
                metricList.add(MetricFamilyBuild.buildGaugeMetric(MetricEnum.NEW_GEN_SPEND,
                        genSpend));
                metricList.add(MetricFamilyBuild.buildGaugeMetric(MetricEnum.OLD_GEN_COUNT,
                        genCount));
                metricList.add(MetricFamilyBuild.buildGaugeMetric(MetricEnum.OLD_GEN_SPEND,
                        genSpend));
            } else if (NEW_GEN_NAME_SET.contains(gcMxBean.getName())) {
                metricList.add(MetricFamilyBuild.buildGaugeMetric(MetricEnum.NEW_GEN_COUNT,
                        genCount));
                metricList.add(MetricFamilyBuild.buildGaugeMetric(MetricEnum.NEW_GEN_SPEND,
                        genSpend));
            } else {
                metricList.add(MetricFamilyBuild.buildGaugeMetric(MetricEnum.OLD_GEN_COUNT,
                        genCount));
                metricList.add(MetricFamilyBuild.buildGaugeMetric(MetricEnum.OLD_GEN_SPEND,
                        genSpend));
            }
        });
    }

    /**
     * fill in the memory metric information
     *
     * @param metricList metric information list
     * @param memoryPoolMxBeans memory metric information
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
     * collect thread metric information
     *
     * @param metricList performance metric information
     * @param threadMxBean thread information
     */
    private void fillThreadMetric(List<MetricFamilySamples> metricList, ThreadMXBean threadMxBean) {
        metricList.add(MetricFamilyBuild.buildGaugeMetric(MetricEnum.THREAD_LIVE, threadMxBean.getThreadCount()));
        metricList.add(MetricFamilyBuild.buildGaugeMetric(MetricEnum.THREAD_DAEMON,
                threadMxBean.getDaemonThreadCount()));
        metricList.add(MetricFamilyBuild.buildGaugeMetric(MetricEnum.THREAD_PEAK, threadMxBean.getPeakThreadCount()));
    }

    /**
     * fill memory metric
     *
     * @param metricFamilySamplesList metric information list
     * @param type type
     * @param memoryUsage metric information
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
