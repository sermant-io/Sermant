/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.route.common.factory;

import java.util.concurrent.ThreadFactory;

/**
 * 命名线程工厂
 *
 * @author zhouss
 * @since 2021-11-01
 */
public class NamedThreadFactory implements ThreadFactory {
    private final String threadName;

    public NamedThreadFactory(String threadName) {
        this.threadName = threadName;
    }

    @Override
    public Thread newThread(Runnable runnable) {
        return new Thread(runnable, threadName);
    }
}
