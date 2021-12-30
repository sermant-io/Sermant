/*
 * Copyright (C) 2021-2021 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huawei.flowrecord.utils;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 单例模式创建redisson处理录制数据线程池
 *
 */
public class RedissonProcessThreadPool {
    private static RedissonProcessThreadPool instance;

    private static final int QUEUE_CAPACITY = 100;

    private static final long KEEP_ALIVE_TIME = 60L;

    private ExecutorService executorService;

    private final int availableProcessor = Runtime.getRuntime().availableProcessors();

    // 用单例模式创建线程池，保留两个核心线程，最多线程为CPU个数
    private RedissonProcessThreadPool() {
        if (executorService == null) {
            int coreNum = availableProcessor / 2;

            int maxProcessor = availableProcessor * 2;

            executorService = new ThreadPoolExecutor(Math.min(coreNum, 2), maxProcessor, KEEP_ALIVE_TIME,
                TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(QUEUE_CAPACITY), new RedissonThreadFactory("redisson"));
        }
    }

    public static RedissonProcessThreadPool getInstance() {
        if (instance == null) {
            instance = new RedissonProcessThreadPool();
        }
        return instance;
    }

    public void executeTask(Runnable runable) {
        executorService.execute(runable);
    }
}
