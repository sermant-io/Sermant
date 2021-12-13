/*
 * Copyright (C) 2021-2021 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huawei.flowrecordreplay.console.rtc.common.redis;

import com.huawei.flowrecordreplay.console.rtc.common.utils.CommonTools;
import com.huawei.flowrecordreplay.console.rtc.common.utils.RtcCoreConstants;

import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisException;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.codec.StringCodec;
import io.lettuce.core.support.AsyncConnectionPoolSupport;
import io.lettuce.core.support.AsyncPool;
import io.lettuce.core.support.BoundedPoolConfig;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.util.JedisClusterCRC16;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
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
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;

/**
 * redis工具类
 *
 * @author hanpeng
 * @since 2021-04-07
 */
@Component
@EnableScheduling
@EnableAsync()
public class RedisUtil implements ApplicationContextAware {
    /**
     * 日志处理对象
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(RedisUtil.class);
    /**
     * 设置定时器时间
     */
    private static final int SCHEDULE_TIME = 1000;
    /**
     * 管道数据批量发送时，最大耗时
     */
    private static final int MAX_WAITING_TIME = 1000;
    /**
     * 设置线程池线程数
     */
    private static final int THREAD_NUM = 10;
    /**
     * 重连频率
     */
    private static final long RECONNECT_INTERVAL_MILLIS = 120000L;
    /**
     * redis客户端对象
     */
    private static RedisClient client;
    /**
     * redis异步连接池
     */
    private static AsyncPool<StatefulRedisConnection<String, String>> pool;
    /**
     * redis主机名
     */
    private static String redisHost;
    /**
     * redis主机端口
     */
    private static int redisPort;
    /**
     * redis是否集群
     */
    private static boolean isCluster;
    /**
     * redis集群节点，可以多个，逗号隔开
     */
    private static String redisClusterNodes;
    /**
     * 使用pipeline时一次发送的量
     */
    private static int pipelineNum;

    /**
     * jedisClusterPipeline对象
     */
    private static JedisClusterPipeline jedisClusterPipeline;

    /**
     * jedisSlotAdvancedConnectionHandler对象
     */
    private static JedisSlotAdvancedConnectionHandler jedisSlotAdvancedConnectionHandler;
    /**
     * jedis异步操作set时需要的锁
     */
    private static final Object SET_LOCK = new Object();
    /**
     * jedis异步操作hash时需要的锁
     */
    private static final Object HASH_LOCK = new Object();
    /**
     * jedis异步操作zset时需要的锁
     */
    private static final Object ZSET_LOCK = new Object();
    /**
     * jedis连接池配置中最大连接数
     */
    private static int maxTotal;
    /**
     * jedis连接池配置中最大空闲连接数
     */
    private static int maxIdle;
    /**
     * jedis连接池配置中最大等待时间
     */
    private static int maxWaitMillis;
    /**
     * jedis连接池配置中最大重试次数
     */
    private static int maxAttempts;
    /**
     * jedis异步操作hash时，定义的数据容器
     */
    private static final ConcurrentHashMap<String, Map<String, String>> HASH_DATA_MAP = new ConcurrentHashMap<>();
    /**
     * jedis异步操作hash时，每次调用时的时间戳
     */
    private static volatile long hashUpdateTime = 0L;
    /**
     * jedis异步操作时，需要的连接池
     */
    private static final ExecutorService ASYNC_PIPELINE_POOL = new ThreadPoolExecutor(THREAD_NUM, THREAD_NUM,
            0L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<>(),
            new CustomizableThreadFactory("jedis-asyncPipeline-threadPool-"));
    /**
     * jedis异步操作set时，每次调用时的时间戳
     */
    private static volatile long setUpdateTime = 0L;
    /**
     * jedis异步操作set时，定义的数据容器
     */
    private static final Map<String, List<String>> SET_DATA_MAP = new ConcurrentHashMap<>();
    /**
     * jedis异步操作Zset时，定义的数据容器
     */
    private static final Map<String, Map<String, Double>> ZSET_DATA_MAP = new ConcurrentHashMap<>();
    /**
     * jedis异步操作Zset时，每次调用时的时间戳
     */
    private static volatile long zsetUpdateTime = 0L;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource(name = "stringRedisTemplate")
    private ValueOperations<String, String> stringRedisValue;

    @Resource(name = "stringRedisTemplate")
    private HashOperations<String, String, String> stringRedisHash;

    @Resource(name = "stringRedisTemplate")
    private SetOperations<String, String> stringRedisSet;

    @Resource(name = "stringRedisTemplate")
    private ZSetOperations<String, String> stringRedisZset;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    private boolean isAlive = Boolean.TRUE;

    private Date reconnectTime = new Date(System.currentTimeMillis());

    private ApplicationContext applicationContext;

    @Value("${pipelineNum}")
    public void setPipelineNum(int pipelineNum) {
        RedisUtil.pipelineNum = pipelineNum;
    }

    @Value("${spring.redis.host}")
    public void setRedisHost(String redisHost) {
        RedisUtil.redisHost = redisHost;
    }

    @Value("${spring.redis.port}")
    public void setRedisPort(int redisPort) {
        RedisUtil.redisPort = redisPort;
    }

    @Value("${spring.redis.isCluster}")
    public void setIsCluster(boolean isCluster) {
        RedisUtil.isCluster = isCluster;
    }

    @Value("${spring.redis.cluster.nodes}")
    public void setRedisClusterNodes(String redisClusterNodes) {
        RedisUtil.redisClusterNodes = redisClusterNodes;
    }

    @Value("${jedisPoolConfig.setMaxTotal}")
    public void setMaxTotal(int maxTotal) {
        RedisUtil.maxTotal = maxTotal;
    }

    @Value("${jedisPoolConfig.setMaxIdle}")
    public void setMaxIdle(int maxIdle) {
        RedisUtil.maxIdle = maxIdle;
    }

    @Value("${jedisPoolConfig.setMaxWaitMillis}")
    public void setMaxWaitMillis(int maxWaitMillis) {
        RedisUtil.maxWaitMillis = maxWaitMillis;
    }

    @Value("${jedisPoolConfig.maxAttempts}")
    public void setMaxAttempts(int maxAttempts) {
        RedisUtil.maxAttempts = maxAttempts;
    }

    /**
     * 初始化连接池
     */
    @PostConstruct
    public void init() {
        if (!isCluster) {
            initSinglePool();
        } else {
            initClusterPool();
        }
    }

    /**
     * 初始化单机Redis异步连接池
     */
    public static void initSinglePool() {
        client = RedisClient.create();

        // 创建异步连接池
        pool = AsyncConnectionPoolSupport.createBoundedObjectPool(
            () -> client.connectAsync(StringCodec.UTF8, RedisURI.create(redisHost, redisPort)),

            // 使用默认的连接池配置
            BoundedPoolConfig.create());
    }

    /**
     * 初始化集群Redis连接池
     */
    public static void initClusterPool() {
        // redis节点集合
        Set<HostAndPort> redisNodes = new HashSet<>();
        String[] nodes = redisClusterNodes.split(",");
        for (String node : nodes) {
            if (node == null || "".equals(node)) {
                continue;
            }

            // 做ip:port校验
            boolean isValid = CommonTools.validateHostAndPort(node);
            if (!isValid) {
                continue;
            }
            String[] hostAndPort = node.split(":");
            redisNodes.add(new HostAndPort(hostAndPort[0], Integer.parseInt(hostAndPort[1])));
        }
        if (redisNodes.size() <= 0) {
            LOGGER.error("The number of the valid ip:port about the redis cluster node in config file is 0");
            return;
        }
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        jedisPoolConfig.setMaxTotal(maxTotal);
        jedisPoolConfig.setMaxIdle(maxIdle);
        jedisPoolConfig.setMaxWaitMillis(maxWaitMillis);
        jedisClusterPipeline = new JedisClusterPipeline(redisNodes, maxAttempts, null, jedisPoolConfig);
        jedisSlotAdvancedConnectionHandler = jedisClusterPipeline.getConnectionHandler();
    }

    /**
     * 异步设置hash
     *
     * @param hkey  键
     * @param key   属性
     * @param value 值
     */
    public static void asyncSetHash(String hkey, String key, String value) {
        if (!isCluster) {
            // 从连接池中获取连接
            CompletableFuture<StatefulRedisConnection<String, String>> con = RedisUtil.pool.acquire();

            // 异步执行hset命令并返回结果
            con.thenCompose(connection -> connection.async().hset(hkey, key, value)

                    // 释放连接池获取的连接
                    .whenComplete((s, throwable) -> RedisUtil.pool.release(connection)));
        } else {
            synchronized (HASH_LOCK) {
                Map<String, String> subMap = HASH_DATA_MAP.computeIfAbsent(hkey, field -> new ConcurrentHashMap<>());
                subMap.put(key, value);
            }
            if (HASH_DATA_MAP.size() >= pipelineNum) {
                hashUpdateTime = System.currentTimeMillis();
                sendHashData(true);
            }
        }
    }

    /**
     * 定时任务：定时发送hash数据到redis
     */
    @Scheduled(fixedRate = SCHEDULE_TIME)
    public static void hashSchedule() {
        sendHashData(false);
    }

    /**
     * 发送hash数据到redis
     *
     * @param isInitiative 是否主动发送，false：表示定时器发送；true：表示管道批量发送达到预设值进行发送
     */
    public static void sendHashData(boolean isInitiative) {
        if (!isCluster && !isInitiative && (System.currentTimeMillis() - hashUpdateTime) < MAX_WAITING_TIME) {
            return;
        }
        final Map<String, Map<String, String>> snapshot;
        synchronized (HASH_LOCK) {
            snapshot = getSnapshot(HASH_DATA_MAP);
        }
        if (!CollectionUtils.isEmpty(snapshot)) {
            ASYNC_PIPELINE_POOL.execute(() -> hashToPipeline(snapshot));
        }
    }

    private static void hashToPipeline(Map<String, Map<String, String>> snapshot) {
        Map<Integer, Map<String, Map<String, String>>> mapBySlot = groupBySlot(snapshot);

        // 调用Jedis pipeline进行单点批量写入
        Set<Map.Entry<Integer, Map<String, Map<String, String>>>> entries = mapBySlot.entrySet();
        for (Map.Entry<Integer, Map<String, Map<String, String>>> entry : entries) {
            JedisPool jedisPool = jedisSlotAdvancedConnectionHandler.getJedisPoolFromSlot(entry.getKey());
            Jedis jedis = null;
            try {
                jedis = jedisPool.getResource();
                if (jedis == null) {
                    LOGGER.error("Failed to get jedis when executing the method hashToPipeline.");
                    return;
                }
                Pipeline pipelined = jedis.pipelined();
                Set<Map.Entry<String, Map<String, String>>> valueEntries = entry.getValue().entrySet();
                for (Map.Entry<String, Map<String, String>> valueEntry : valueEntries) {
                    pipelined.hmset(valueEntry.getKey(), valueEntry.getValue());
                    pipelined.expire(valueEntry.getKey(), RtcCoreConstants.METRIC_EXPIRE_TIME);
                }
                pipelined.sync();
            } finally {
                if (jedis != null) {
                    jedis.close();
                }
            }
        }
    }

    /**
     * 定时任务：定时发送数据到redis
     */
    @Scheduled(fixedRate = SCHEDULE_TIME)
    public static void setSchedule() {
        sendSetData(false);
    }

    /**
     * 异步设置set
     *
     * @param key   键
     * @param value 值
     */
    public static void asyncSetSet(String key, String value) {
        if (!isCluster) {
            // 从连接池中获取连接
            CompletableFuture<StatefulRedisConnection<String, String>> con = pool.acquire();

            // 异步执行setex命令并返回结果
            con.thenCompose(connection -> connection.async().sadd(key, value)

                    // 释放连接池获取的连接
                    .whenComplete((s, throwable) -> pool.release(connection)));
        } else {
            synchronized (SET_LOCK) {
                List<String> list = SET_DATA_MAP.computeIfAbsent(key, field -> new ArrayList<>());
                list.add(value);
            }
            if (SET_DATA_MAP.size() >= pipelineNum) {
                setUpdateTime = System.currentTimeMillis();
                sendSetData(true);
            }
        }
    }

    /**
     * 发送数据到redis
     *
     * @param isInitiative 是否主动发送，false：表示定时器发送；true：表示管道批量发送达到预设值进行发送
     */
    public static void sendSetData(boolean isInitiative) {
        if (!isCluster && !isInitiative && (System.currentTimeMillis() - setUpdateTime) < MAX_WAITING_TIME) {
            return;
        }
        Map<String, List<String>> snapshot;
        synchronized (SET_LOCK) {
            snapshot = getSnapshot(SET_DATA_MAP);
        }
        if (!CollectionUtils.isEmpty(snapshot)) {
            ASYNC_PIPELINE_POOL.execute(() -> setSetToPipeline(snapshot));
        }
    }

    private static void setSetToPipeline(Map<String, List<String>> snapshot) {
        Map<Integer, Map<String, List<String>>> mapBySlot = groupBySlot(snapshot);

        // 调用Jedis pipeline进行单点批量写入
        Set<Map.Entry<Integer, Map<String, List<String>>>> entries = mapBySlot.entrySet();
        for (Map.Entry<Integer, Map<String, List<String>>> entry : entries) {
            JedisPool jedisPool = jedisSlotAdvancedConnectionHandler.getJedisPoolFromSlot(entry.getKey());
            Jedis jedis = null;
            try {
                jedis = jedisPool.getResource();
                if (jedis == null) {
                    LOGGER.error("Failed to get jedis when executing the method setSetToPipeline.");
                    return;
                }
                Pipeline pipelined = jedis.pipelined();
                Set<Map.Entry<String, List<String>>> valueEntries = entry.getValue().entrySet();
                for (Map.Entry<String, List<String>> valueEntry : valueEntries) {
                    pipelined.sadd(valueEntry.getKey(), valueEntry.getValue().toArray(new String[0]));
                }
                pipelined.sync();
            } finally {
                if (jedis != null) {
                    jedis.close();
                }
            }
        }
    }

    /**
     * 定时任务：定时发送zset数据到redis
     */
    @Scheduled(fixedRate = SCHEDULE_TIME)
    public static void zsetSchedule() {
        sendZsetData(false);
    }

    /**
     * 异步设置Zset
     *
     * @param key   键
     * @param value 值
     * @param score 分值
     */
    public static void asyncZadd(String key, String value, double score) {
        if (!isCluster) {
            // 从连接池中获取连接
            CompletableFuture<StatefulRedisConnection<String, String>> con = pool.acquire();

            // 异步执行setex命令并返回结果
            con.thenCompose(connection -> connection.async().zadd(key, score, value)
                    // 释放连接池获取的连接
                    .whenComplete((s, throwable) -> pool.release(connection)));
        } else {
            synchronized (ZSET_LOCK) {
                Map<String, Double> map = ZSET_DATA_MAP.computeIfAbsent(key, field -> new HashMap<>());
                map.put(value, score);
            }
            if (ZSET_DATA_MAP.size() >= pipelineNum) {
                zsetUpdateTime = System.currentTimeMillis();
                sendZsetData(true);
            }
        }
    }

    /**
     * 发送set数据
     *
     * @param isInitiative 是否主动发送，false：表示定时器发送；true：表示管道批量发送达到预设值进行发送
     */
    public static void sendZsetData(boolean isInitiative) {
        if (!isCluster && !isInitiative && (System.currentTimeMillis() - zsetUpdateTime) < MAX_WAITING_TIME) {
            return;
        }
        Map<String, Map<String, Double>> snapshot;
        synchronized (ZSET_LOCK) {
            snapshot = getSnapshot(ZSET_DATA_MAP);
        }
        if (!CollectionUtils.isEmpty(snapshot)) {
            ASYNC_PIPELINE_POOL.execute(() -> zaddToPipeline(snapshot));
        }
    }

    private static void zaddToPipeline(Map<String, Map<String, Double>> snapshot) {
        Map<Integer, Map<String, Map<String, Double>>> mapBySlot = groupBySlot(snapshot);

        // 调用Jedis pipeline进行单点批量写入
        Set<Map.Entry<Integer, Map<String, Map<String, Double>>>> entries = mapBySlot.entrySet();
        for (Map.Entry<Integer, Map<String, Map<String, Double>>> entry : entries) {
            JedisPool jedisPool = jedisSlotAdvancedConnectionHandler.getJedisPoolFromSlot(entry.getKey());
            Jedis jedis = null;
            try {
                jedis = jedisPool.getResource();
                if (jedis == null) {
                    LOGGER.error("Failed to get jedis when executing the method zaddToPipeline.");
                    return;
                }
                Pipeline pipelined = jedis.pipelined();
                Set<Map.Entry<String, Map<String, Double>>> valueEntries = entry.getValue().entrySet();
                for (Map.Entry<String, Map<String, Double>> valueEntry : valueEntries) {
                    pipelined.zadd(valueEntry.getKey(), valueEntry.getValue());
                    pipelined.expire(valueEntry.getKey(), RtcCoreConstants.METRIC_EXPIRE_TIME);
                }
                pipelined.sync();
            } finally {
                if (jedis != null) {
                    jedis.close();
                }
            }
        }
    }

    /**
     * 根据槽点分组
     *
     * @param map 数据的map集合
     * @param <T> 数据类型
     * @return 返回槽点-数据集合的map集合
     */
    public static <T> Map<Integer, Map<String, T>> groupBySlot(Map<String, T> map) {
        jedisClusterPipeline.refreshCluster();
        Set<Map.Entry<String, T>> entries = map.entrySet();
        Map<Integer, Map<String, T>> poolMap = new HashMap<>();
        for (Map.Entry<String, T> entry : entries) {
            int slot = JedisClusterCRC16.getSlot(entry.getKey());
            Map<String, T> value = poolMap.computeIfAbsent(slot, field -> new HashMap<>());
            value.put(entry.getKey(), entry.getValue());
        }
        return poolMap;
    }

    private static <K, V> Map<K, V> getSnapshot(Map<K, V> map) {
        if (!CollectionUtils.isEmpty(map)) {
            Map<K, V> snapshot = new HashMap<>(map.size());
            snapshot.putAll(map);
            map.clear();
            return snapshot;
        }
        return Collections.emptyMap();
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
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
     * @param key   值
     * @param value 值
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
     * @param key   主键
     * @param field hash字段名
     * @return string
     */
    public String getHash(String key, String field) {
        return stringRedisHash.get(key, field);
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
     * @param key      键
     * @param hashKey  属性key
     * @param value    值
     * @param timeout  过期时间
     * @param timeUnit 时间单位
     */
    public void setHashAndSetExpire(String key, String hashKey, String value, long timeout, TimeUnit timeUnit) {
        try {
            stringRedisHash.put(key, hashKey, value);
            setExpire(key, timeout, timeUnit);
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
        Boolean isExist = stringRedisHash.hasKey(key, field);
        return isExist == null ? false : isExist;
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
    public void zsetAdd(String key, String value, double score) {
        stringRedisZset.add(key, value, score);
    }

    /**
     * 保存数据为zset，按时间排序
     *
     * @param key       键
     * @param value     值
     * @param timeStamp 分值
     */
    public void zsetAdd(String key, String value, long timeStamp) {
        stringRedisZset.add(key, value, timeStamp);
    }

    /**
     * 关闭连接
     */
    public static void close() {
        // 关闭连接池
        if (pool != null) {
            pool.closeAsync();
        }

        // 关闭client
        if (client != null) {
            client.shutdownAsync();
        }
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
        return stringRedisSet.members(key);
    }

    /**
     * 获取zset的中指定范围内存在值的数量
     *
     * @param key 键
     * @param min 范围的较小值
     * @param max 范围的较大值
     * @return 返回存在数量
     */
    public long getZsetCount(String key, double min, double max) {
        return stringRedisZset.count(key, min, max);
    }

    /**
     * 根据key获取所有的fields
     *
     * @param key 键
     * @return set 返回set集合结果
     */
    public Set<String> getHashFieldsByKey(String key) {
        return stringRedisHash.keys(key);
    }

    /**
     * 根据key获取所有的值
     *
     * @param key 键
     * @return list 返回list集合结果
     */
    public List<String> getHashValuesByKey(String key) {
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
     * 根据key，hkey查询数据
     *
     * @param key   键
     * @param field 字段名
     * @return Object 字段结果
     */
    public String getHashValue(String key, String field) {
        return stringRedisHash.get(key, field);
    }

    /**
     * 设置数据过期时间，单位秒
     *
     * @param key      键
     * @param timeout  过期时间
     * @param timeUnit 时间单位
     */
    private void setExpire(String key, long timeout, TimeUnit timeUnit) {
        if (timeout > 0) {
            redisTemplate.expire(key, timeout, timeUnit);
        } else {
            throw new IllegalArgumentException("The timeout value is recommended to be greater than 0.");
        }
    }

    /**
     * key getExpire
     *
     * @param key 过期的键 not null
     * @return time unit second, If it's zero, it's always valid.
     */
    public long getExpire(String key) {
        return redisTemplate.getExpire(key, TimeUnit.SECONDS);
    }

    /**
     * 根据对应的key删除key
     *
     * @param key 键
     * @return true删除成功，反之删除失败
     */
    public boolean delKey(String key) {
        return stringRedisTemplate.delete(key);
    }

    /**
     * 销毁redis工具类对象
     */
    @PreDestroy
    public void destroy() {
        LOGGER.info("Begin to Destroy RedisUtil.");
        RedisConnectionFactory redisConnectionFactory = redisTemplate.getConnectionFactory();
        if (redisConnectionFactory == null) {
            LOGGER.error("ConnectionFactory is null.");
            return;
        }
        if (pool != null) {
            close();
            if (redisConnectionFactory instanceof LettuceConnectionFactory) {
                ((LettuceConnectionFactory) redisConnectionFactory).destroy();
            }
            LOGGER.info("Destroy RedisUtil success.");
        } else {
            LOGGER.error("AsyncPool is null.");
        }
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
                    LettuceConnectionFactory lettuceConnectionFactory =
                            (LettuceConnectionFactory) redisConnectionFactory;
                    lettuceConnectionFactory.destroy();
                }
            }
            setAlive(true);
            LOGGER.info("End to reconnect to redis.");
        }
    }
}