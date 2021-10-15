/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.lubanops.apm.plugin.servermonitor.jvm;

import com.lubanops.apm.plugin.servermonitor.entity.IbmJvmMetric;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryPoolMXBean;
import java.util.List;

/**
 * ibm jdk处理类
 *
 * @author zhengbin zhao
 * @since 2021-03-16
 */
@Deprecated
public enum MemoryPoolProvider {
    /**
     * 实例
     */
    INSTANCE;

    private MemoryPoolMetricsAccessor metricAccessor;
    private List<MemoryPoolMXBean> beans;

    MemoryPoolProvider() {
        beans = ManagementFactory.getMemoryPoolMXBeans();
        for (MemoryPoolMXBean bean : beans) {
            String name = bean.getName();
            MemoryPoolMetricsAccessor accessor = findByBeanName(name);
            if (accessor != null) {
                metricAccessor = accessor;
                break;
            }
        }
        if (metricAccessor == null) {
            metricAccessor = new UnknownMemoryPool();
        }
    }

    /**
     * 获取内存中metric集合
     *
     * @return IbmJvmMetric
     */
    public List<IbmJvmMetric> getMemoryPoolMetricsList() {
        return metricAccessor.getMemoryPoolMetricsList();
    }

    private MemoryPoolMetricsAccessor findByBeanName(String name) {
        if (CheckIBMParameter.returnCheckResult(name)) {
            // ibm jvm collector
            return new IBMJVMCollectorModule(beans);
        } else {
            // Unknown
            return null;
        }
    }
}
