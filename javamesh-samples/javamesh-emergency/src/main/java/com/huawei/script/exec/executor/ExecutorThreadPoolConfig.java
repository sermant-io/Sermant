/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.script.exec.executor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 用于脚本执行的线程池配置
 *
 * @author y30010171
 * @since 2021-10-26
 **/
@Configuration
public class ExecutorThreadPoolConfig {
    private static final String THREAD_NAME_PREFIX = "task-exec-";

    private static final long KEEP_ALIVE_TIME = 60L;

    @Value("${script.executor.maxTaskSize}")
    private int maxTaskSize;

    @Value("${script.executor.maxSubtaskSize}")
    private int maxSubtaskSize;

    @Value("${script.executor.blockingTaskSize}")
    private int blockingTaskSize;

    /**
     * 用于预案，任务，脚本执行的线程池
     * 设置核心线程数与最大线程数一致，使得创建的线程与线程名一致。
     * 每个脚本在不同服务器执行时，根据此线程名去获取一个线程池来并发执行。
     *
     * @return {@link ThreadPoolExecutor}
     */
    @Bean(destroyMethod = "shutdown")
    public ThreadPoolExecutor scriptExecThreadPool() {
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(
            maxTaskSize,
            maxTaskSize,
            KEEP_ALIVE_TIME,
            TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(blockingTaskSize),
            new ThreadFactory() {
                private AtomicInteger threadCount = new AtomicInteger();

                @Override
                public Thread newThread(Runnable r) {
                    return new Thread(r, THREAD_NAME_PREFIX + threadCount.getAndIncrement());
                }
            });
        threadPoolExecutor.allowCoreThreadTimeOut(true);
        return threadPoolExecutor;
    }

    /**
     * 通过此线程池执行设置了超时时间的脚本
     *
     * @return {@link ThreadPoolExecutor}
     */
    @Bean(destroyMethod = "shutdown")
    public ThreadPoolExecutor timeoutScriptExecThreadPool() {
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(
            maxTaskSize * maxSubtaskSize,
            maxTaskSize * maxSubtaskSize,
            KEEP_ALIVE_TIME,
            TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(blockingTaskSize),
            new ThreadFactory() {
                private AtomicInteger threadCount = new AtomicInteger();

                @Override
                public Thread newThread(Runnable r) {
                    return new Thread(r, "timeout-exec-" + threadCount.getAndIncrement());
                }
            });
        threadPoolExecutor.allowCoreThreadTimeOut(true);
        return threadPoolExecutor;
    }

    @Bean("passwordRestTemplate")
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        DefaultUriBuilderFactory defaultUriBuilderFactory = new DefaultUriBuilderFactory();
        defaultUriBuilderFactory.setEncodingMode(DefaultUriBuilderFactory.EncodingMode.NONE);
        restTemplate.setUriTemplateHandler(defaultUriBuilderFactory);
        return restTemplate;
    }
}
