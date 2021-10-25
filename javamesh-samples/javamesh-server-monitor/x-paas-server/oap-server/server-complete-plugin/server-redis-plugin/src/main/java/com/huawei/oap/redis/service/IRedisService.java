/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.oap.redis.service;

import io.lettuce.core.Range;
import io.lettuce.core.ScoredValue;
import org.apache.skywalking.oap.server.library.module.Service;

import java.util.List;
import java.util.Map;

/**
 * redis操作
 *
 * @author hefan
 * @since 2021-06-11
 */
public interface IRedisService extends Service {
    /**
     * get命令
     *
     * @param key 键
     * @return 值
     */
    String get(String key);

    /**
     * set命令
     *
     * @param key 键
     * @param value 值
     * @return 是否成功写入
     */
    void set(String key, String value);

    /**
     * set 加过期时间
     *
     * @param key 键
     * @param value 值
     * @return 数量
     */
    boolean setIfNotExist(String key, String value);

    /**
     * 设置过期时间
     *
     * @param key 键
     * @param second 时间，单位S
     */
    boolean expire(String key, long second);

    /**
     * 获取hash对应的所有值
     *
     * @param key 键
     * @return 值
     */
    Map<String, String> hGetAll(String key);

    /**
     * 删除操作
     *
     * @param key 键
     * @return 删除个数
     */
    long hDel(String key, String... fields);

    /**
     * 设置属性和值
     *
     * @param key 键
     * @param field 属性名
     * @param value 属性值
     */
    void hSet(String key, String field, String value);

    /**
     * 获取分数在范围内的值
     *
     * @param key 键
     * @param range 范围
     * @return 结果集
     */
    List<ScoredValue<String>> zRangeByScoreWithScores(String key, Range<Double> range);

    /**
     * zadd操作
     *
     * @param key 键
     * @param scoredValues 数据对象
     */
    void zAdd(String key, ScoredValue<String>... scoredValues);

    /**
     * 按索引查找zset
     *
     * @param key 键
     * @param start 开始索引
     * @param stop 结束索引
     * @return 结果集
     */
    List<ScoredValue<String>> zRangeWithScore(String key, long start, long stop);

    /**
     * 删除成员
     *
     * @param key 键
     * @param members 成员
     */
    void zRemove(String key, String... members);

    /**
     * 获取过期时间
     *
     * @param key
     * @return
     */
    long ttl(String key);

    /**
     * 删除键
     *
     * @param key 键
     * @return 删除数量
     */
    long del(String... key);
}
