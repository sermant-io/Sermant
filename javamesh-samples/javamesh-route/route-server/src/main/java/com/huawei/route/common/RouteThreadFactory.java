/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.route.common;

import java.util.concurrent.ThreadFactory;

/**
 * apm线程工厂
 *
 * @author zhouss
 * @since 2021/10/9 14:59
 */
public class RouteThreadFactory implements ThreadFactory {
    private final String threadName;

    public RouteThreadFactory(String threadName) {
        this.threadName = threadName;
    }

    @Override
    public Thread newThread(Runnable runnable) {
        return new Thread(runnable, threadName);
    }
}
