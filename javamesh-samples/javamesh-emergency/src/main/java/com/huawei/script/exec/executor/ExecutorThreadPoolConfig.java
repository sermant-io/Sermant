/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.script.exec.executor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 用于脚本执行的线程池配置
 *
 * @author y30010171
 * @since 2021-10-26
 **/
@Configuration
public class ExecutorThreadPoolConfig {
    @Value("${script.executor.corePoolSize}")
    private int coreSize;

    @Value("${script.executor.maxPoolSize}")
    private int maxSize;

    @Value("${script.executor.keepAliveTime}")
    private long keepAliveTime;

    @Value("${script.executor.blockingQueueSize}")
    private int blockingQueueSize;

    @Bean(destroyMethod = "shutdown")
    public ThreadPoolExecutor scriptExecThreadPool() {
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(
            coreSize, maxSize, keepAliveTime, TimeUnit.SECONDS, new ArrayBlockingQueue<>(blockingQueueSize));
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
