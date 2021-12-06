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

package com.huawei.flowcontrol.core.util;

import com.alibaba.csp.sentinel.log.RecordLog;
import com.huawei.javamesh.core.plugin.config.PluginConfigManager;
import com.huawei.flowcontrol.core.config.CommonConst;
import com.huawei.flowcontrol.core.config.FlowControlConfig;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;

/**
 * 流控插件连接zookeeper的单例类
 *
 * @author Zhang Hu
 * @since 2020-12-10
 */
public enum ZookeeperConnectionEnum {
    /**
     * 生成zk连接的实例
     */
    INSTANCE;

    private CuratorFramework client;

    ZookeeperConnectionEnum() {
        final FlowControlConfig flowControlConfig = PluginConfigManager.getPluginConfig(FlowControlConfig.class);
        RecordLog.info("start connect zookeeper, address: [{}]", flowControlConfig.getZookeeperAddress());

        // 创建 CuratorFrameworkImpl实例
        try {
            client = CuratorFrameworkFactory.newClient(flowControlConfig.getZookeeperAddress(),
                new ExponentialBackoffRetry(CommonConst.SLEEP_TIME, CommonConst.RETRY_TIMES));
            client.start();
        } catch (Exception e) {
            RecordLog.error("create zk client failed, please check your config!", e);
        }
    }

    /**
     * 获取zk连接
     *
     * @return CuratorFramework zk客户端
     */
    public CuratorFramework getZookeeperConnection() {
        return client;
    }
}
