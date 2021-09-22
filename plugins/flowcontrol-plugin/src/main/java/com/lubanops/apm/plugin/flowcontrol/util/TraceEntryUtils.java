/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.lubanops.apm.plugin.flowcontrol.util;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.Tracer;

/**
 * 记录流控追踪信息工具类
 *
 * @author Zhang Hu
 * @since 2021-04-25
 */
public class TraceEntryUtils {
    private TraceEntryUtils() {
    }

    public static void traceEntry(ThreadLocal<Entry> consumerThreadLocal,
        ThreadLocal<Entry> providerThreadLocal,
        Throwable throwable) {
        Entry consumerEntry = consumerThreadLocal.get();
        if (consumerEntry != null) {
            if (throwable != null) {
                Tracer.traceEntry(throwable, consumerEntry);
            }
        }

        Entry providerEntry = providerThreadLocal.get();
        if (providerEntry != null) {
            if (throwable != null) {
                Tracer.traceEntry(throwable, providerEntry);
            }
        }
    }
}
