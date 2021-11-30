/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.flowrecordreplay.console.rtc.common.redis;

import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisCluster;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

import java.util.Set;

/**
 * redis客户端jedis集群的pipeline模式类
 *
 * @author hanpeng
 * @since 2021-04-07
 */
public class JedisClusterPipeline extends JedisCluster {
    private static final int CONNECTION_TIMEOUT = 20000;
    private static final int SO_TIMEOUT = 2000;

    /**
     * 初始化jedis
     *
     * @param jedisClusterNode 集群节点
     * @param maxAttempts 最大重试次数
     * @param password 密码
     * @param poolConfig 连接池配置
     */
    public JedisClusterPipeline(Set<HostAndPort> jedisClusterNode,
                                int maxAttempts, String password,
                                final GenericObjectPoolConfig poolConfig) {
        super(jedisClusterNode, CONNECTION_TIMEOUT,
                SO_TIMEOUT, maxAttempts, password, poolConfig);
        super.connectionHandler = new JedisSlotAdvancedConnectionHandler(
                jedisClusterNode, poolConfig,
                CONNECTION_TIMEOUT, SO_TIMEOUT, password);
    }

    /**
     * 获取集群槽点
     *
     * @return 返回连接
     */
    public JedisSlotAdvancedConnectionHandler getConnectionHandler() {
        return (JedisSlotAdvancedConnectionHandler) this.connectionHandler;
    }

    /**
     * 刷新集群信息，当集群信息发生变更时调用
     */
    public void refreshCluster() {
        connectionHandler.renewSlotCache();
    }
}
