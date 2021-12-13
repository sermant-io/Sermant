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

package com.huawei.flowcontrol.console.datasource.entity.rule.zookeeper;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * zookeeper配置类
 * test包中有相同命名，导致命名冲突，需重命名
 *
 * @author XiaoLong Wang
 * @since 2020-12-21
 */
@Configuration("flowcontrolZookeeperConfig")
public class ZookeeperConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger(ZookeeperConfig.class);

    /**
     * zookeeper连接超时时间，默认（60000）
     */
    private static final int CONNECTION_TIMEOUT_MS = 5000;

    /**
     * zookeeper会话超时时间，默认（60000）
     */
    private static final int SESSION_TIMEOUT_MS = 5000;

    /**
     * 重连次数
     */
    private static final int RETRY_TIMES = 1;

    /**
     * 重连时间间隔
     */
    private static final int SLEEP_TIME = 1000;

    @Value("${zookeeper.client}")
    private String zookeeperclient;

    /**
     * 连接zookeeper
     *
     * @return curatorFramework
     */
    @Bean
    public CuratorFramework zkClient() {
        // 连接zk
        CuratorFramework zkClient = null;
        try {
            zkClient = CuratorFrameworkFactory.newClient(zookeeperclient, SESSION_TIMEOUT_MS, CONNECTION_TIMEOUT_MS,
                new ExponentialBackoffRetry(SLEEP_TIME, RETRY_TIMES));
            LOGGER.info("Create zookeeper session success.");
            zkClient.start();
        } catch (Exception e) {
            LOGGER.error("Create zookeeper session failed.", e);
        }

        return zkClient;
    }
}
