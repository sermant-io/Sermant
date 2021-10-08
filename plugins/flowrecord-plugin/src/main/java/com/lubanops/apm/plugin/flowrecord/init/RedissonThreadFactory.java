/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.lubanops.apm.plugin.flowrecord.init;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * redisson处理录制数据线程池工厂类
 *
 * @author lilai
 * @version 0.0.1
 * @since 2021-05-12
 */
public class RedissonThreadFactory implements ThreadFactory {
    private final AtomicInteger poolNumber = new AtomicInteger(1);
    private final AtomicInteger threadNumber = new AtomicInteger(1);
    private final String namePrefix;
    private final ThreadGroup group;

    public RedissonThreadFactory(String name) {
        SecurityManager securityManager = System.getSecurityManager();
        group = (securityManager != null) ? securityManager.getThreadGroup() : Thread.currentThread().getThreadGroup();
        namePrefix = name + "-" + poolNumber.getAndIncrement() + "-thread-";
    }

    @Override
    public Thread newThread(Runnable r) {
        Thread thread = new Thread(group, r, namePrefix + threadNumber.getAndIncrement(), 0);
        if (thread.isDaemon()) {
            thread.setDaemon(false);
        }
        if (thread.getPriority() != Thread.NORM_PRIORITY) {
            thread.setPriority(Thread.NORM_PRIORITY);
        }
        return thread;
    }
}
