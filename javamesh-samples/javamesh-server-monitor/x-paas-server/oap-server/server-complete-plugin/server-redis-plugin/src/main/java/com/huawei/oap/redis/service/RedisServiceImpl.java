/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.oap.redis.service;

import io.lettuce.core.Range;
import io.lettuce.core.ScoredValue;
import io.lettuce.core.api.StatefulConnection;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import io.lettuce.core.cluster.api.StatefulRedisClusterConnection;
import io.lettuce.core.cluster.api.sync.RedisAdvancedClusterCommands;

import java.util.List;
import java.util.Map;

/**
 * redis操作实现
 *
 * @author hefan
 * @since 2021-06-11
 */
public class RedisServiceImpl implements IRedisService {
    private final boolean isCluster;

    private StatefulRedisClusterConnection<String, String> clusterConnection;

    private StatefulRedisConnection<String, String> connection;

    public RedisServiceImpl(StatefulConnection<String, String> connect) {
        if (connect instanceof StatefulRedisClusterConnection) {
            this.clusterConnection = (StatefulRedisClusterConnection<String, String>) connect;
            isCluster = true;
        } else if (connect instanceof StatefulRedisConnection) {
            this.connection = (StatefulRedisConnection<String, String>) connect;
            isCluster = false;
        } else {
            throw new IllegalArgumentException("Error redis connection type!");
        }
    }

    @Override
    public String get(String key) {
        if (isCluster) {
            return clusterConnection.sync().get(key);
        } else {
            return connection.sync().get(key);
        }
    }

    @Override
    public void set(String key, String value) {
        if (isCluster) {
            clusterConnection.sync().set(key, value);
        } else {
            connection.sync().set(key, value);
        }
    }

    @Override
    public boolean setIfNotExist(String key, String value) {
        if (isCluster) {
            return clusterConnection.sync().setnx(key, value);
        } else {
            return connection.sync().setnx(key, value);
        }
    }

    @Override
    public boolean expire(String key, long second) {
        if (isCluster) {
            return clusterConnection.sync().expire(key, second);
        } else {
            return connection.sync().expire(key, second);
        }
    }

    @Override
    public Map<String, String> hGetAll(String key) {
        if (isCluster) {
            return clusterConnection.sync().hgetall(key);
        } else {
            return connection.sync().hgetall(key);
        }
    }

    @Override
    public long hDel(String key, String... fields) {
        if (isCluster) {
            return clusterConnection.sync().hdel(key, fields);
        } else {
            return connection.sync().hdel(key, fields);
        }
    }

    @Override
    public void hSet(String key, String field, String value) {
        if (isCluster) {
            clusterConnection.sync().hset(key, field, value);
        } else {
            connection.sync().hset(key, field, value);
        }
    }

    @Override
    public List<ScoredValue<String>> zRangeByScoreWithScores(String key, Range<Double> range) {
        if (isCluster) {
            return clusterConnection.sync().zrangebyscoreWithScores(key, range);
        } else {
            return connection.sync().zrangebyscoreWithScores(key, range);
        }
    }

    @Override
    public void zAdd(String key, ScoredValue<String>... scoredValues) {
        if (isCluster) {
            clusterConnection.sync().zadd(key, scoredValues);
        } else {
            connection.sync().zadd(key, scoredValues);
        }
    }

    @Override
    public List<ScoredValue<String>> zRangeWithScore(String key, long start, long stop) {
        if (isCluster) {
            return clusterConnection.sync().zrangeWithScores(key, start, stop);
        } else {
            return connection.sync().zrangeWithScores(key, start, stop);
        }
    }

    @Override
    public void zRemove(String key, String... members) {
        if (isCluster) {
            clusterConnection.sync().zrem(key, members);
        } else {
            connection.sync().zrem(key, members);
        }
    }

    @Override
    public long ttl(String key) {
        if (isCluster) {
            return clusterConnection.sync().ttl(key);
        } else {
            return connection.sync().ttl(key);
        }
    }

    @Override
    public long del(String... key) {
        if (isCluster) {
            return clusterConnection.sync().del(key);
        } else {
            return connection.sync().del(key);
        }
    }
}
