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

package com.huawei.flowre.flowreplay.config;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * zookeeper 工具用于获取注册中心注册信息
 *
 * @author luanwenfei
 * @version 0.0.1
 * @since 2021-03-03
 */
@Configuration
public class ZookeeperConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger(ZookeeperConfig.class);

    /**
     * zookeeper地址
     */
    @Value("${zookeeper.url}")
    private String zookeeperUrl;

    /**
     * zookeeper 连接超时时间
     */
    @Value("${zookeeper.connect.timeout}")
    private int zookeeperConnectTimeout;

    /**
     * zookeeper 会话超时时间
     */
    @Value("${zookeeper.session.timeout}")
    private int zookeeperSessionTimeout;

    /**
     * zookeeper 连接重试次数
     */
    @Value("${zookeeper.retry.times}")
    private int zookeeperRetryTimes;

    /**
     * zookeeper 重试间隔
     */
    @Value("${zookeeper.sleep.time}")
    private int zookeeperSleepTime;

    @Bean
    public CuratorFramework zkClient() {
        CuratorFramework zkClient = null;
        try {
            zkClient = CuratorFrameworkFactory.builder()
                    .connectString(zookeeperUrl)
                    .connectionTimeoutMs(zookeeperConnectTimeout)
                    .sessionTimeoutMs(zookeeperSessionTimeout)
                    .retryPolicy(new ExponentialBackoffRetry(zookeeperSleepTime, zookeeperRetryTimes)).build();
            zkClient.start();
        } catch (Exception exception) {
            LOGGER.error("Create zookeeper session failed. {} address: ", exception.getMessage());
        }
        return zkClient;
    }
}
