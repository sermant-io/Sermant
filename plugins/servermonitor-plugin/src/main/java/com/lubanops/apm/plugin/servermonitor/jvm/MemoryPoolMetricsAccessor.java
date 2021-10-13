/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.lubanops.apm.plugin.servermonitor.jvm;


import com.lubanops.apm.plugin.servermonitor.entity.IBMMemoryPool;

import java.util.List;

/**
 * ibm jdk存取类
 *
 * @author zhengbin zhao
 * @since 2021-03-16
 */
public interface MemoryPoolMetricsAccessor {
    List<IBMMemoryPool> getMemoryPoolMetricsList();
}
