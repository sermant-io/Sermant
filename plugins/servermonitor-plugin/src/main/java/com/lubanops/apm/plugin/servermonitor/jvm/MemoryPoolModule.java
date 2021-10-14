/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.lubanops.apm.plugin.servermonitor.jvm;

import com.lubanops.apm.plugin.servermonitor.entity.IBMMemoryPool;
import com.lubanops.apm.plugin.servermonitor.entity.IBMPoolType;

import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryUsage;
import java.util.LinkedList;
import java.util.List;

/**
 * ibm jdk模块
 *
 * @author zhengbin zhao
 * @since 2021-03-16
 */
public abstract class MemoryPoolModule implements MemoryPoolMetricsAccessor {
    private List<MemoryPoolMXBean> beans;

    public MemoryPoolModule(List<MemoryPoolMXBean> beans) {
        this.beans = beans;
    }

    @Override
    public List<IBMMemoryPool> getMemoryPoolMetricsList() {
        List<IBMMemoryPool> poolList = new LinkedList<IBMMemoryPool>();
        for (MemoryPoolMXBean bean : beans) {
            String name = bean.getName();
            IBMPoolType type;
            if (CheckIBMParameter.returnCheckResult(name)) {
                type = CheckIBMParameter.returnPoolType(name);
            } else {
                continue;
            }

            MemoryUsage usage = bean.getUsage();
            IBMMemoryPool ibmMemoryPool = IBMMemoryPool.newBuilder()
                .setType(type)
                .setInit(usage.getInit())
                .setMax(usage.getMax())
                .setCommitted(usage.getCommitted())
                .setUsed(usage.getUsed()).build();

            poolList.add(ibmMemoryPool);
        }
        return poolList;
    }
}
