/*
 * Copyright (C) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.huawei.javamesh.core.lubanops.integration.transport.netty.client;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
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

    // 线程缓冲队列大小
    private static int queueSize = 1000;

    // 线程缓冲队列
    private static BlockingQueue<Runnable> blockingQueue = new LinkedBlockingQueue<>(queueSize);

    // ThreadPoolExecutor线程池对象
    public static ThreadPoolExecutor exec = new ThreadPoolExecutor(SIZE_CORE_POOL, SIZE_MAX_POOL, ALIVE_TIME,
            TimeUnit.MILLISECONDS, blockingQueue, Thread::new, new ThreadPoolExecutor.AbortPolicy());

    public static ThreadPoolExecutor getExecutor() {
        return exec;
    }
}
