/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.route.server.rules.notifier;

import com.huawei.route.server.conditions.ZookeeperConfigCenterCondition;
import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

/**
 * 路径数据更新器
 *
 * @author zhouss
 * @since 2021-10-28
 */
@Component
@Conditional(ZookeeperConfigCenterCondition.class)
public class PathDataUpdater {
    private static final Logger LOGGER = LoggerFactory.getLogger(PathDataUpdater.class);
    @Autowired
    private CuratorFramework zkClient;

    /**
     * 更新ZK路径的数据
     *
     * @param path zk路劲
     * @param data 数据
     */
    public void updatePathData(String path, byte[] data) {
        try {
            final Stat stat = zkClient.checkExists().forPath(path);
            if (data == null) {
                data = String.valueOf(System.currentTimeMillis()).getBytes(StandardCharsets.UTF_8);
            }
            if (stat == null) {
                // 节点为空
                zkClient.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT)
                        .forPath(path, data);
            } else {
                zkClient.setData().forPath(path, data);
            }
        } catch (Exception e) {
            LOGGER.warn("update path data failed for notification");
        }
    }
}
