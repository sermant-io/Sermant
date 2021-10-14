/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.lubanops.apm.plugin.servermonitor.jvm;

import java.lang.management.MemoryPoolMXBean;
import java.util.List;

/**
 * ibm jvm 信息采集类
 *
 * @author zhengbin zhao
 * @since 2021-03-16
 */
public class IBMJVMCollectorModule extends MemoryPoolModule {
    public IBMJVMCollectorModule(List<MemoryPoolMXBean> beans) {
        super(beans);
    }
}
