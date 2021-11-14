/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.javamesh.sample.servermonitor.collector;

import com.huawei.apm.core.lubanops.bootstrap.log.LogFactory;
import com.huawei.javamesh.sample.servermonitor.common.CheckIBMParameter;
import com.huawei.javamesh.sample.servermonitor.entity.IbmPoolMetric;
import com.huawei.javamesh.sample.servermonitor.entity.IbmPoolType;

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
 * 重构泛PaaS：com.huawei.apm.plugin.collection.jvm.MemoryPoolModule
 */
public class IbmJvmMetricCollector {

    private static final Logger LOGGER = LogFactory.getLogger();

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
