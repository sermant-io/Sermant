/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.flowrecordreplay.console.rtc.common.redis;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;

/**
 * redis复合模式配置类
 *
 * @author hanpeng
 * @since 2021-04-07
 */
@Configuration
public class RedisConfig {
    /**
     * 自动注入redis连接工厂
     */
    @Autowired
    private RedisConnectionFactory redisConnectionFactory;

    /**
     * 往IOC容器中注册stringRedisTemplate对象
     *
     * @return StringRedisTemplate
     */
    @Bean("stringRedisTemplate")
    public StringRedisTemplate redisStringTemplate() {
        StringRedisTemplate stringRedisTemplate = new StringRedisTemplate();
        stringRedisTemplate.setConnectionFactory(this.redisConnectionFactory);
        return stringRedisTemplate;
    }

    /**
     * 往IOC容器中注册RedisTemplate对象
     *
     * @return RedisTemplate
     */
    @Bean("redisTemplate")
    public RedisTemplate<String, Object> redisTemplate() {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(this.redisConnectionFactory);
        RedisSerializer<String> stringSerializer = redisTemplate.getStringSerializer();
        redisTemplate.setKeySerializer(stringSerializer);
        return redisTemplate;
    }
}