/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.gray.feign.context;

/**
 * 下游服务名线程变量
 *
 * @author lilai
 * @since 2021-11-03
 */
public class HostContext {
    /**
     * 下游服务名
     */
    private static final ThreadLocal<String> HOST_CONTEXT = new ThreadLocal<String>();

    public static void set(String hostContext) {
        HOST_CONTEXT.set(hostContext);
    }

    public static String get() {
        return HOST_CONTEXT.get();
    }

    public static void remove() {
        HOST_CONTEXT.remove();
    }
}
