/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.lubanops.apm.plugin.flowrecord.init;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 单例模式创建redisson处理录制数据线程池
 *
 * @author lilai
 * @version 0.0.1
 * @since 2021-05-12
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
                TimeUnit.SECONDS, new ArrayBlockingQueue<>(QUEUE_CAPACITY), new RedissonThreadFactory("redisson"));
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
