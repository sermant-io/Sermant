/*
 * Copyright (C) 2024-2024 Sermant Authors. All rights reserved.
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

package io.sermant.backend.dao.redis;

import io.sermant.backend.common.conf.BackendConfig;
import io.sermant.backend.common.conf.CommonConst;
import redis.clients.jedis.CommandObjects;
import redis.clients.jedis.Connection;
import redis.clients.jedis.ConnectionPool;
import redis.clients.jedis.ConnectionPoolConfig;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisClientConfig;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.resps.Tuple;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Redis cluster client
 *
 * @author zhp
 * @since 2024-06-25
 */
public class RedisClusterOperationImpl implements RedisOperation {
    private static final Logger LOGGER = LoggerFactory.getLogger(RedisClusterOperationImpl.class);

    private static final int MAX_ATTEMPTS = 5;

    private final CommandObjects commandObjects = new CommandObjects();

    private JedisCluster jedisCluster;

    /**
     * Construct the Jedis Cluster
     *
     * @param backendConfig configuration
     * @param addressArray Redis cluster address
     */
    public RedisClusterOperationImpl(BackendConfig backendConfig, String[] addressArray) {
        Set<HostAndPort> hostAndPortSet = new HashSet<>(addressArray.length);
        for (String address : addressArray) {
            String[] addressInfo = address.split(CommonConst.REDIS_ADDRESS_SPLIT);
            if (addressInfo.length <= 1) {
                LOGGER.warn("Redis connection address configuration error");
                return;
            }
            hostAndPortSet.add(new HostAndPort(addressInfo[0], Integer.parseInt(addressInfo[1])));
        }
        if (CollectionUtils.isEmpty(hostAndPortSet)) {
            LOGGER.error("Redis connection address configuration error");
            return;
        }
        JedisClientConfig jedisClientConfig = new JedisClientConfig() {
            public String getUser() {
                return backendConfig.getUser();
            }

            public String getPassword() {
                return backendConfig.getPassword();
            }

            public int getConnectionTimeoutMillis() {
                return Integer.parseInt(backendConfig.getTimeout());
            }
        };
        ConnectionPoolConfig connectionPoolConfig = new ConnectionPoolConfig();
        connectionPoolConfig.setMaxTotal(Integer.parseInt(backendConfig.getMaxTotal()));
        connectionPoolConfig.setMaxIdle(Integer.parseInt(backendConfig.getMaxIdle()));
        jedisCluster = new JedisCluster(hostAndPortSet, jedisClientConfig, MAX_ATTEMPTS, connectionPoolConfig);
    }

    @Override
    public Set<String> keys(String pattern) {
        Map<String, ConnectionPool> clusterNodes = jedisCluster.getClusterNodes();
        Set<String> keys = new HashSet<>();
        for (Map.Entry<String, ConnectionPool> entry : clusterNodes.entrySet()) {
            try (Connection connection = entry.getValue().getResource()) {
                Set<String> res = connection.executeCommand(commandObjects.keys(pattern));
                if (!CollectionUtils.isEmpty(res)) {
                    keys.addAll(res);
                }
            }
        }
        return keys;
    }

    @Override
    public String setex(String key, long seconds, String value) {
        return jedisCluster.setex(key, seconds, value);
    }

    @Override
    public String psetex(String key, long milliseconds, String value) {
        return jedisCluster.psetex(key, milliseconds, value);
    }

    @Override
    public long hset(String key, String field, String value) {
        return jedisCluster.hset(key, field, value);
    }

    @Override
    public long zadd(String key, double score, String member) {
        return jedisCluster.zadd(key, score, member);
    }

    @Override
    public List<Tuple> zrangeByScoreWithScores(String key, double min, double max) {
        return jedisCluster.zrangeByScoreWithScores(key, min, max);
    }

    @Override
    public String get(String key) {
        return jedisCluster.get(key);
    }

    @Override
    public String hget(String key, String field) {
        return jedisCluster.hget(key, field);
    }

    @Override
    public long hdel(String key, String[] fields) {
        return jedisCluster.hdel(key, fields);
    }

    @Override
    public long zrem(String key, String[] members) {
        return jedisCluster.zrem(key, members);
    }
}
