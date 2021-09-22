/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2021. All rights reserved.
 */

package com.lubanops.apm.plugin.flowcontrol.core.util;

import com.alibaba.csp.sentinel.log.RecordLog;
import com.huawei.apm.bootstrap.config.ConfigLoader;
import com.lubanops.apm.plugin.flowcontrol.core.config.CommonConst;
import com.lubanops.apm.plugin.flowcontrol.core.config.FlowControlConfig;
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
        final FlowControlConfig flowControlConfig = ConfigLoader.getConfig(FlowControlConfig.class);
        RecordLog.info("start connect zookeeper, address: [{}]", flowControlConfig.getSentinelZookeeperAddress());

        // 创建 CuratorFrameworkImpl实例
        try {
            client = CuratorFrameworkFactory.newClient(flowControlConfig.getSentinelZookeeperAddress(),
                new ExponentialBackoffRetry(CommonConst.SLEEP_TIME, CommonConst.RETRY_TIMES));
        } catch (IllegalArgumentException e) {
            RecordLog.error("create acl zk client failed, please check your acl config!", e);
        }
        client.start();
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
