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
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.resps.Tuple;

import java.util.List;
import java.util.Set;

/**
 * Redis stand-alone client
 *
 * @author xuezechao
 * @since 2024-06-25
 */
public class RedisStandAloneOperationImpl implements RedisOperation {
    private final JedisPool jedisPool;

    /**
     * Construct the Jedis connection pool
     *
     * @param backendConfig configuration
     */
    public RedisStandAloneOperationImpl(BackendConfig backendConfig) {
        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxTotal(Integer.parseInt(backendConfig.getMaxTotal()));
        config.setMaxIdle(Integer.parseInt(backendConfig.getMaxIdle()));
        String[] addressInfo = backendConfig.getUrl().split(CommonConst.REDIS_ADDRESS_SPLIT);
        if (backendConfig.getVersion().compareTo("6.0") < 0) {
            jedisPool = new JedisPool(
                    config, addressInfo[0], Integer.parseInt(addressInfo[1]),
                    Integer.parseInt(backendConfig.getTimeout()), backendConfig.getPassword());
        } else {
            jedisPool = new JedisPool(
                    config,
                    addressInfo[0],
                    Integer.parseInt(addressInfo[1]),
                    Integer.parseInt(backendConfig.getTimeout()),
                    backendConfig.getUser(),
                    backendConfig.getPassword());
        }
    }

    @Override
    public Set<String> keys(String pattern) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.keys(pattern);
        }
    }

    @Override
    public String setex(String key, long seconds, String value) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.setex(key, seconds, value);
        }
    }

    @Override
    public String psetex(String key, long milliseconds, String value) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.psetex(key, milliseconds, value);
        }
    }

    @Override
    public long hset(String key, String field, String value) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.hset(key, field, value);
        }
    }

    @Override
    public long zadd(String key, double score, String member) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.zadd(key, score, member);
        }
    }

    @Override
    public List<Tuple> zrangeByScoreWithScores(String key, double min, double max) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.zrangeByScoreWithScores(key, min, max);
        }
    }

    @Override
    public String get(String key) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.get(key);
        }
    }

    @Override
    public String hget(String key, String field) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.hget(key, field);
        }
    }

    @Override
    public long hdel(String key, String[] fields) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.hdel(key, fields);
        }
    }

    @Override
    public long zrem(String key, String[] members) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.zrem(key, members);
        }
    }
}
