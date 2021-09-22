/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.lubanops.apm.integration.transport.netty.client;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 将数据缓存进消息队列的线程池
 *
 * @author lilai
 * @version 0.0.1
 * @since 2021-08-07
 */
public class ThreadPools {
    // 核心线程数，会一直存活，即使没有任务，线程池也会维护线程的最少数量
    private static final int SIZE_CORE_POOL = 5;

    // 线程池维护线程的最大数量
    private static final int SIZE_MAX_POOL = 10;

    // 线程池维护线程所允许的空闲时间
    private static final long ALIVE_TIME = 2000L;

    // 线程缓冲队列
    private static BlockingQueue<Runnable> bqueue = new LinkedBlockingQueue<Runnable>(1000);

    // ThreadPoolExecutor线程池对象
    private static ThreadPoolExecutor exec = new ThreadPoolExecutor(SIZE_CORE_POOL, SIZE_MAX_POOL, ALIVE_TIME,
            TimeUnit.MILLISECONDS, bqueue, new ThreadFactory() {
                @Override
                public Thread newThread(Runnable r) {
                    return new Thread(r);
                }
            }, new ThreadPoolExecutor.AbortPolicy());

    public static ThreadPoolExecutor getExecutor() {
        return exec;
    }
}
