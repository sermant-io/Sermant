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

import com.huawei.sermant.core.common.LoggerFactory;
import com.huawei.sermant.plugin.servermonitor.common.CheckIBMParameter;
import com.huawei.sermant.plugin.servermonitor.entity.IbmPoolMetric;
import com.huawei.sermant.plugin.servermonitor.entity.IbmPoolType;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryUsage;
import java.util.EnumMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * IBM JVM 采集器
 *
 * 重构泛PaaS：com.huawei.sermant.plugin.collection.jvm.MemoryPoolModule
 */
public class IbmJvmMetricCollector {

    private static final Logger LOGGER = LoggerFactory.getLogger();

    private final Map<IbmPoolType, MemoryPoolMXBean> memoryPoolMxBeans =
        new EnumMap<IbmPoolType, MemoryPoolMXBean>(IbmPoolType.class);

    public IbmJvmMetricCollector() {
        for (MemoryPoolMXBean mxBean : ManagementFactory.getMemoryPoolMXBeans()) {
            String name = mxBean.getName();
            if (CheckIBMParameter.CLASS_STORAGE.getParameter().equals(name)) {
                memoryPoolMxBeans.put(IbmPoolType.IBM_CLASS_STORAGE_USAGE, mxBean);
            } else if (CheckIBMParameter.MISCELLANEOUS.getParameter().equals(name)) {
                memoryPoolMxBeans.put(IbmPoolType.IBM_MISCELLANEOUS_USAGE, mxBean);
            } else if (CheckIBMParameter.NURSERY_ALLOCATE.getParameter().equals(name)) {
                memoryPoolMxBeans.put(IbmPoolType.IBM_NURSERY_ALLOCATE_USAGE, mxBean);
            } else if (CheckIBMParameter.NURSERY_SURVIVOR.getParameter().equals(name)) {
                memoryPoolMxBeans.put(IbmPoolType.IBM_NURSERY_SURVIVOR_USAGE, mxBean);
            } else if (CheckIBMParameter.TENURED_LOA.getParameter().equals(name)) {
                memoryPoolMxBeans.put(IbmPoolType.IBM_TENURED_LOA_USAGE, mxBean);
            } else if (CheckIBMParameter.TENURED_SOA.getParameter().equals(name)) {
                memoryPoolMxBeans.put(IbmPoolType.IBM_TENURED_SOA_USAGE, mxBean);
            } else if (CheckIBMParameter.CODE_CACHE.getParameter().equals(name)) {
                memoryPoolMxBeans.put(IbmPoolType.IBM_CODE_CACHE_USAGE, mxBean);
            } else if (CheckIBMParameter.DATA_CACHE.getParameter().equals(name)) {
                memoryPoolMxBeans.put(IbmPoolType.IBM_DATA_CACHE_USAGE, mxBean);
            }
        }
        if (memoryPoolMxBeans.isEmpty()) {
            LOGGER.warning("No ibm memory pool found.");
        }
    }

    public List<IbmPoolMetric> getIbmJvmMetrics() {
        List<IbmPoolMetric> jvmMetrics = new LinkedList<IbmPoolMetric>();
        for (Map.Entry<IbmPoolType, MemoryPoolMXBean> typeAndBeanEntry : memoryPoolMxBeans.entrySet()) {
            final MemoryUsage memoryUsage = typeAndBeanEntry.getValue().getUsage();
            IbmPoolMetric ibmPoolMetric = IbmPoolMetric.newBuilder()
                .setType(typeAndBeanEntry.getKey())
                .setInit(memoryUsage.getInit())
                .setMax(memoryUsage.getMax())
                .setCommitted(memoryUsage.getCommitted())
                .setUsed(memoryUsage.getUsed())
                .build();
            jvmMetrics.add(ibmPoolMetric);
        }
        return jvmMetrics;
    }
}
