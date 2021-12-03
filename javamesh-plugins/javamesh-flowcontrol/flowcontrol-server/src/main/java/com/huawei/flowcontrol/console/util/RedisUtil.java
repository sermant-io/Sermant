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

package com.huawei.flowcontrol.console.util;

import io.lettuce.core.RedisException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import java.time.Duration;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * redis工具类
 *
 * @author wl
 * @since 2020-12-21
 */
@Component
public class RedisUtil implements ApplicationContextAware {
    /**
     * 日志处理对象
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(RedisUtil.class);

    /**
     * 重连频率
     */
    private static final long RECONNECT_INTERVAL_MILLIS = 120000L;

    private StringRedisTemplate stringRedisTemplate;

    @Resource(name = "stringRedisTemplate")
    private ValueOperations<String, String> stringRedisValue;

    @Resource(name = "stringRedisTemplate")
    private HashOperations<String, String, String> stringRedisHash;

    @Resource(name = "stringRedisTemplate")
    private SetOperations<String, String> stringRedisSet;

    @Resource(name = "stringRedisTemplate")
    private ZSetOperations<String, String> stringRedisZset;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private boolean isAlive = Boolean.TRUE;

    private Date reconnectTime = new Date(System.currentTimeMillis());

    private ApplicationContext applicationContext;

    @Value("${redis.data.timeout}")
    private long timeout;

    @Value("${redis.rule.data.timeout}")
    private long ruleTimeout;

    @Autowired
    public void setStringRedisTemplate(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Override
    public void setApplicationContext(@NonNull ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    /**
     * Specify the expired time
     *
     * @param key  过期的键
     * @param time 过期的时间,单位秒
     */
    @ExceptionHandler
    public void setExpire(String key, long time) {
        if (time > 0) {
            stringRedisTemplate.expire(key, time, TimeUnit.SECONDS);
        } else {
            LOGGER.warn("setExpire failed: time should be more than zero!");
        }
    }

    /**
     * 设置数据过去时间，单位天
     *
     * @param key 键
     */
    private void setExpire(String key) {
        if (timeout != 0) {
            stringRedisTemplate.expire(key, timeout, TimeUnit.DAYS);
        }
    }

    /**
     * key getExpire
     *
     * @param key 过期的键 not null
     * @return time unit second, If it's zero, it's always valid.
     */
    public long getExpire(String key) {
        return stringRedisTemplate.getExpire(key, TimeUnit.SECONDS);
    }

    /**
     * Determine if the key exists
     *
     * @param key 键名
     * @return true:exist; false:not exist
     */
    public boolean hasKey(String key) {
        Boolean isExist = stringRedisTemplate.hasKey(key);
        return isExist == null ? false : isExist;
    }

    /**
     * get value
     *
     * @param key 键名
     * @return value 值名
     */
    public String get(String key) {
        return key == null || "".equals(key) ? "" : stringRedisValue.get(key);
    }

    /**
     * set key value
     *
     * @param key   键
     * @param value 值
     */
    public void set(String key, String value) {
        stringRedisValue.set(key, value);
    }

    /**
     * set key value and expired time
     *
     * @param key   保存redis主键
     * @param value 保存redis中的值
     * @param time  过期时间(s)
     */
    public void set(String key, String value, long time) {
        if (time > 0) {
            stringRedisValue.set(key, value, time, TimeUnit.MILLISECONDS);
        } else {
            set(key, value);
        }
    }

    /**
     * delete key
     *
     * @param keys 键的字符串集合
     */
    public void del(String[] keys) {
        stringRedisTemplate.delete(Arrays.asList(keys));
    }

    /**
     * get hash
     *
     * @param hashKey 主键
     * @param key     hash字段名
     * @return string
     */
    public String getHash(String hashKey, String key) {
        return stringRedisHash.get(hashKey, key);
    }

    /**
     * get hash map
     *
     * @param hashKey hash主键
     * @return map map
     */

    public Map<String, String> getHashMap(String hashKey) {
        return stringRedisHash.entries(hashKey);
    }

    /**
     * set hash
     *
     * @param hashKey hash主键
     * @param key     hash字段名
     * @param value   值
     */
    public void setHash(String hashKey, String key, String value) {
        stringRedisHash.put(hashKey, key, value);
    }

    /**
     * 存放hash
     *
     * @param key          键
     * @param hashKey      属性key
     * @param value        值
     * @param isSecondUnit 是否设置过期时间，true为是，false为否
     */
    public void setHash(String key, String hashKey, String value, boolean isSecondUnit) {
        try {
            stringRedisHash.put(key, hashKey, value);
            if (isSecondUnit) {
                // 设置数据过期时间,单位秒
                setExpireSeconds(key);
            } else {
                // 设置数据过期时间，单位天
                setExpire(key);
            }
        } catch (RedisException e) {
            LOGGER.error("failed to save hash.", e);
        }
    }

    /**
     * set hash map
     *
     * @param hashKey hash主键
     * @param map     键值对map集合
     */
    public void setHashMap(String hashKey, Map<String, String> map) {
        stringRedisHash.putAll(hashKey, map);
    }

    /**
     * delete hash
     *
     * @param hashKey hash主键
     * @param key     键值对map集合
     */
    public void delHash(String hashKey, String key) {
        stringRedisHash.delete(hashKey, key);
    }

    /**
     * set has key
     *
     * @param key   键
     * @param field 属性
     * @return 返回是否存在key值的布尔值
     */
    public boolean isExistHashKey(String key, String field) {
        return stringRedisHash.hasKey(key, field);
    }

    /**
     * set set
     *
     * @param key   键
     * @param value 值
     */
    public void setSet(String key, String value) {
        stringRedisSet.add(key, value);
    }

    /**
     * 设置zset
     *
     * @param key   键
     * @param value 值
     * @param score 分值
     */
    public void addzSet(String key, String value, long score) {
        stringRedisZset.add(key, value, score);
    }

    /**
     * 保存数据为zset，按时间排序
     *
     * @param key       键
     * @param value     值
     * @param timeStamp 分值
     */
    public void zSetAdd(String key, String value, long timeStamp) {
        stringRedisZset.add(key, value, timeStamp);

        // 设置数据过期时间
        setExpire(key);
    }

    /**
     * 删除zset中的某个元素
     *
     * @param key   键
     * @param value 值
     */
    public synchronized void delzSetEle(String key, String value) {
        stringRedisZset.remove(key, value);
    }

    /**
     * 根据key查询指定分数范围的元素
     *
     * @param key   键
     * @param start 开始偏移量
     * @param end   结束偏移量
     * @return 返回set数据
     */
    public Set<String> getzSetScore(String key, long start, long end) {
        return stringRedisZset.reverseRange(key, start, end);
    }

    /**
     * 返回zset数据集合中包含元素个数
     *
     * @param key 键
     * @return long 返回long类型总数
     */
    public long getZcard(String key) {
        return stringRedisZset.zCard(key);
    }

    /**
     * delete set
     *
     * @param key   键
     * @param value 值
     */
    public void delSet(String key, String value) {
        stringRedisSet.remove(key, value);
    }

    /**
     * get set
     *
     * @param key 键
     * @return 返回set集合
     */
    public Set<String> getSet(String key) {
        Set<String> dataSet = stringRedisSet.members(key);
        return dataSet == null ? new HashSet<>() : dataSet;
    }

    /**
     * 获取zset的中指定范围内存在值的数量
     *
     * @param key 键
     * @param min 范围的较小值
     * @param max 范围的较大值
     * @return 返回存在数量
     */
    public long getZsetCount(@NonNull String key, double min, double max) {
        return stringRedisZset.count(key, min, max);
    }

    /**
     * 根据key查询时间范围数据
     *
     * @param key   键
     * @param start 开始偏移量
     * @param end   结束偏移量
     * @return 返回set数据
     */
    public Set<String> getzSet(String key, long start, long end) {
        return stringRedisZset.rangeByScore(key, start, end);
    }

    /**
     * 根据key获取所有的filed
     *
     * @param key 键
     * @return set 返回set集合
     */
    public Set<String> getHashField(String key) {
        return stringRedisHash.keys(key);
    }

    /**
     * 根据key获取所有的值
     *
     * @param key 键
     * @return list list集合
     */
    public List<String> getHashValues(String key) {
        return stringRedisHash.values(key);
    }

    /**
     * 根据key获取entry
     *
     * @param key 键
     * @return map map集合
     */
    public Map<String, String> getHashEntriesByKey(String key) {
        return stringRedisHash.entries(key);
    }

    /**
     * 根据key，field查询数据
     *
     * @param key   键
     * @param field 字段名
     * @return String 字段结果
     */
    public String queryForValue(String key, String field) {
        return stringRedisHash.get(key, field);
    }

    /**
     * 设置数据过期时间，单位秒
     *
     * @param key 键
     */
    private void setExpireSeconds(@NonNull String key) {
        if (ruleTimeout != 0) {
            stringRedisTemplate.expire(key, ruleTimeout, TimeUnit.SECONDS);
        }
    }

    /**
     * 根据对应的key删除key
     *
     * @param key 键
     * @return true删除成功，反之删除失败
     */
    public boolean delKey(@NonNull String key) {
        return stringRedisTemplate.delete(key);
    }

    /**
     * 前缀获取所有符合条件的key
     *
     * @param key 键
     * @return true删除成功，反之删除失败
     */
    public Set<String> keys(@NonNull String key) {
        return stringRedisTemplate.keys(key);
    }

    /**
     * 批量查询，对应mget
     *
     * @param keys key的集合
     * @return List
     */
    public List<String> mget(Set<String> keys) {
        return stringRedisTemplate.opsForValue().multiGet(keys);
    }

    /**
     * 销毁redis工具类对象
     */
    @PreDestroy
    public void destroy() {
        LOGGER.info("Begin to destroy RedisUtil.");
        RedisConnectionFactory redisConnectionFactory = redisTemplate.getConnectionFactory();
        if (redisConnectionFactory == null) {
            LOGGER.error("ConnectionFactory is null.");
            return;
        }
        if (redisConnectionFactory instanceof LettuceConnectionFactory) {
            ((LettuceConnectionFactory) redisConnectionFactory).destroy();
        }
        LOGGER.info("Destroy RedisUtil success.");
    }

    /**
     * 设置redis集群的状态
     *
     * @param isValid true为有效，false为无效
     */
    public synchronized void setAlive(Boolean isValid) {
        Date current = new Date(System.currentTimeMillis());
        LOGGER.info("Begin set redis cluster state:{}.", isValid);
        if (!isValid) {
            if (current.getTime() - reconnectTime.getTime() > RECONNECT_INTERVAL_MILLIS) {
                LOGGER.info("Set redis cluster state:{}.", false);
                isAlive = Boolean.FALSE;
            }
        } else {
            reconnectTime = current;
            LOGGER.info("Set redis cluster state:{}.", true);
            isAlive = Boolean.TRUE;
        }
    }

    /**
     * 检查redis集群的状态
     */
    public void checkState() {
        if (!isAlive) {
            LOGGER.info("Begin to reconnect to redis.");
            RedisConnectionFactory redisConnectionFactory = redisTemplate.getConnectionFactory();
            redisTemplate.setConnectionFactory(applicationContext.getBean(RedisConnectionFactory.class));
            if (redisConnectionFactory == null) {
                LOGGER.error("ConnectionFactory is null.");
            } else {
                if (redisConnectionFactory instanceof LettuceConnectionFactory) {
                    LettuceConnectionFactory lettuceConnectionFactory
                        = (LettuceConnectionFactory) redisConnectionFactory;
                    lettuceConnectionFactory.destroy();
                }
            }
            setAlive(true);
            LOGGER.info("End to reconnect to redis.");
        }
    }

    /**
     * 不存在则设置值
     *
     * @param key     键
     * @param value   值
     * @param timeout 超时时间，毫秒
     * @return 不存在则设置值，并返回true，存在则返回false
     */
    public boolean setIfAbsent(String key, String value, long timeout) {
        return Optional.ofNullable(redisTemplate.opsForValue().setIfAbsent(key, value, Duration.ofMillis(timeout)))
            .orElse(false);
    }
}
