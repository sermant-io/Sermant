/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.route.server.labels.util;

import org.apache.curator.framework.CuratorFramework;

/**
 * zookeeper工具类
 *
 * @author Zhang Hu
 * @since 2021-04-09
 */
public class ZookeeperUtil {
    private ZookeeperUtil() {
    }

    /**
     * 检查改节点是否存在
     *
     * @param curatorFramework zookeeper连接
     * @param path             zookeeper中的node节点
     * @return boolean 如果存在则为true，否则为false
     * @throws Exception 判断过程中出现任何异常
     */
    public static boolean isNodeExist(CuratorFramework curatorFramework, String path) throws Exception {
        return curatorFramework.checkExists().forPath(path) != null;
    }
}
