/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.flowrecordreplay.console.rtc.common.redis;

import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisSlotBasedConnectionHandler;
import redis.clients.jedis.exceptions.JedisNoReachableClusterNodeException;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

import java.util.Set;

/**
 * 连接处理器
 *
 * @author hudeyu
 * @since 2021-04-07
 */
public class JedisSlotAdvancedConnectionHandler extends JedisSlotBasedConnectionHandler {
    public JedisSlotAdvancedConnectionHandler(Set<HostAndPort> nodes,
        GenericObjectPoolConfig poolConfig,
        int connectionTimeout, int soTimeout, String password) {
        super(nodes, poolConfig, connectionTimeout, soTimeout, password);
    }

    /**
     * 获取集群槽点
     *
     * @param slot 槽点
     * @return 返回连接池
     */
    public JedisPool getJedisPoolFromSlot(int slot) {
        JedisPool connectionPool = cache.getSlotPool(slot);
        if (connectionPool != null) {
            return connectionPool;
        } else {
            renewSlotCache();
            connectionPool = cache.getSlotPool(slot);
            if (connectionPool != null) {
                return connectionPool;
            } else {
                throw new JedisNoReachableClusterNodeException("No reachable node in cluster for slot " + slot);
            }
        }
    }
}
