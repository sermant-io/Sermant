/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.flowre.flowreplay.utils;

import com.huawei.flowre.flowreplay.config.Const;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;

/**
 * zookeeper 工具类
 *
 * @author luanwenfei
 * @version 1.0
 * @since 2021-03-15
 */
public class ZookeeperUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(ZookeeperUtil.class);

    private ZookeeperUtil() {
    }

    /**
     * 向节点添加数据
     *
     * @param path       节点路径
     * @param object     节点数据
     * @param zkClient   zk客户端
     * @param createMode 选择永久节点或临时节点
     * @return 返回添加节点是否成功
     */
    public static boolean setNode(String path, Object object, CuratorFramework zkClient, CreateMode createMode) {
        try {
            // 判断路径是否存在，不存在则创建
            Stat stat = zkClient.checkExists().forPath(path);

            // 临时节点残留，则删除原来的临时节点
            if (stat != null && createMode.equals(CreateMode.EPHEMERAL)) {
                zkClient.delete().forPath(path);
            }

            if (stat == null) {
                byte[] data = object == null ? JSON.toJSONString("[]").getBytes(StandardCharsets.UTF_8)
                        : JSON.toJSONString(object).getBytes(StandardCharsets.UTF_8);
                zkClient.create().creatingParentContainersIfNeeded().withMode(createMode).forPath(path, data);
                return true;
            }
        } catch (Exception exception) {
            LOGGER.error("Zookeeper client set node error : {}", exception.getMessage());
            return false;
        }
        return false;
    }

    /**
     * 获取节点数据
     *
     * @param path     节点路径
     * @param zkClient zk客户端
     * @return 返回结果字符串
     */
    public static String getData(String path, CuratorFramework zkClient) {
        try {
            Stat stat = zkClient.checkExists().forPath(path);
            if (stat == null) {
                return Const.BLANK;
            }
            byte[] bytes = zkClient.getData().forPath(path);
            if (bytes == null || bytes.length == 0) {
                return Const.BLANK;
            }
            return new String(bytes);
        } catch (Exception exception) {
            LOGGER.error("Zookeeper client get data error : {}", exception.getMessage());
        }
        return Const.BLANK;
    }

    /**
     * 设置节点数据
     *
     * @param path     节点路径
     * @param object   节点数据
     * @param zkClient zk客户端
     */
    public static void setData(String path, Object object, CuratorFramework zkClient) {
        // 判断路径是否存在，不存在则创建
        try {
            Stat stat = zkClient.checkExists().forPath(path);
            if (stat == null) {
                zkClient.create().creatingParentContainersIfNeeded().forPath(path);
            }

            byte[] data = object == null ? "[]".getBytes(StandardCharsets.UTF_8)
                    : JSONObject.toJSONString(object).getBytes(StandardCharsets.UTF_8);
            zkClient.setData().forPath(path, data);
        } catch (Exception exception) {
            LOGGER.error("Zookeeper client set data error : {}", exception.getMessage());
        }
    }

    /**
     * 删除节点
     *
     * @param path     节点路径
     * @param zkClient zk客户端
     */
    public static void deleteNode(String path, CuratorFramework zkClient) {
        try {
            zkClient.delete().forPath(path);
        } catch (Exception exception) {
            LOGGER.error("Zookeeper client delete node error : {}", exception.getMessage());
        }
    }
}
