/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.flowrecordreplay.console.datasource.zookeeper;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * zookeeper连接配置
 *
 * @author lilai
 * @version 0.0.1
 * @since 2021-02-26
 */
@Configuration
public class ZookeeperConfig {
    /**
     * 重连次数
     */
    public static final int RETRY_TIMES = 1;

    /**
     * 重连时间间隔
     */
    public static final int SLEEP_TIME = 1000;

    /**
     * zookeeper连接超时时间，默认（60000）
     */
    private static final int CONNECTION_TIMEOUT_MS = 5000;

    /**
     * zookeeper会话超时时间，默认（60000）
     */
    private static final int SESSION_TIMEOUT_MS = 5000;

    private static final Logger LOGGER = LoggerFactory.getLogger(ZookeeperConfig.class);

    @Value("${zookeeper.client}")
    private String zookeeperUrl;

    @Bean
    public CuratorFramework zkClient() {
        // 连接zookeeper
        CuratorFramework zkClient = null;
        try {
            LOGGER.info("Connecting to zookeeper address...");
            zkClient = CuratorFrameworkFactory.builder().connectString(zookeeperUrl)
                    .connectionTimeoutMs(CONNECTION_TIMEOUT_MS)
                    .sessionTimeoutMs(SESSION_TIMEOUT_MS)
                    .retryPolicy(new ExponentialBackoffRetry(SLEEP_TIME, RETRY_TIMES))
                    .build();

            zkClient.start();
            LOGGER.info("FlowRecord-Replay zookeeper address = {}", zookeeperUrl);
        } catch (Exception e) {
            LOGGER.error("connect zookeeper config center failed. address: {}", zookeeperUrl, e);
        }

        return zkClient;
    }
}
