/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.lubanops.apm.plugin.servermonitor.collector;

import com.lubanops.apm.plugin.servermonitor.entity.IbmJvmMetric;
import com.lubanops.apm.plugin.servermonitor.entity.IBMPoolType;
import com.lubanops.apm.plugin.servermonitor.jvm.CheckIBMParameter;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryUsage;
import java.util.EnumMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * IBM JVM 采集器
 *
 * 重构泛PaaS：com.huawei.apm.plugin.collection.jvm.MemoryPoolModule
 */
public class IbmJvmMetricCollector {

    private final Map<IBMPoolType, MemoryPoolMXBean> memoryPoolMxBeans =
        new EnumMap<IBMPoolType, MemoryPoolMXBean>(IBMPoolType.class);

    public IbmJvmMetricCollector() {
        for (MemoryPoolMXBean mxBean : ManagementFactory.getMemoryPoolMXBeans()) {
            String name = mxBean.getName();
            if (CheckIBMParameter.CLASS_STORAGE.getParameter().equals(name)) {
                memoryPoolMxBeans.put(IBMPoolType.IBM_CLASS_STORAGE_USAGE, mxBean);
            } else if (CheckIBMParameter.MISCELLANEOUS.getParameter().equals(name)) {
                memoryPoolMxBeans.put(IBMPoolType.IBM_MISCELLANEOUS_USAGE, mxBean);
            } else if (CheckIBMParameter.NURSERY_ALLOCATE.getParameter().equals(name)) {
                memoryPoolMxBeans.put(IBMPoolType.IBM_NURSERY_ALLOCATE_USAGE, mxBean);
            } else if (CheckIBMParameter.NURSERY_SURVIVOR.getParameter().equals(name)) {
                memoryPoolMxBeans.put(IBMPoolType.IBM_NURSERY_SURVIVOR_USAGE, mxBean);
            } else if (CheckIBMParameter.TENURED_LOA.getParameter().equals(name)) {
                memoryPoolMxBeans.put(IBMPoolType.IBM_TENURED_LOA_USAGE, mxBean);
            } else if (CheckIBMParameter.TENURED_SOA.getParameter().equals(name)) {
                memoryPoolMxBeans.put(IBMPoolType.IBM_TENURED_SOA_USAGE, mxBean);
            } else if (CheckIBMParameter.CODE_CACHE.getParameter().equals(name)) {
                memoryPoolMxBeans.put(IBMPoolType.IBM_CODE_CACHE_USAGE, mxBean);
            } else if (CheckIBMParameter.DATA_CACHE.getParameter().equals(name)) {
                memoryPoolMxBeans.put(IBMPoolType.IBM_DATA_CACHE_USAGE, mxBean);
            }
        }
        if (memoryPoolMxBeans.isEmpty()) {
            // LOGGER.warn("No ibm memory pool found.");
        }
    }

    public List<IbmJvmMetric> getIbmJvmMetrics() {
        List<IbmJvmMetric> jvmMetrics = new LinkedList<IbmJvmMetric>();
        for (Map.Entry<IBMPoolType, MemoryPoolMXBean> typeAndBeanEntry : memoryPoolMxBeans.entrySet()) {
            final MemoryUsage memoryUsage = typeAndBeanEntry.getValue().getUsage();
            IbmJvmMetric ibmJvmMetric = IbmJvmMetric.newBuilder()
                .setType(typeAndBeanEntry.getKey())
                .setInit(memoryUsage.getInit())
                .setMax(memoryUsage.getMax())
                .setCommitted(memoryUsage.getCommitted())
                .setUsed(memoryUsage.getUsed())
                .build();
            jvmMetrics.add(ibmJvmMetric);
        }
        return jvmMetrics;
    }
}
