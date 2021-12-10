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

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettucePoolingClientConfiguration;

import java.time.Duration;

/**
 * redis单机连接类
 *
 * @author hanpeng
 * @since 2021-04-07
 */
@Configuration
public class RedisConn {
    @Value("${spring.redis.host:localhost}")
    private String host;

    @Value("${spring.redis.port:6379}")
    private int port;

    @Value("${spring.redis.password:}")
    private String password;

    @Value("${spring.redis.timeout:10000}")
    private int timeout;

    @Value("${spring.redis.database:0}")
    private int database;

    @Value("${spring.redis.lettuce.pool.max-active:100}")
    private int maxActive;

    @Value("${spring.redis.lettuce.pool.max-wait:10}")
    private int maxWait;

    @Value("${spring.redis.lettuce.pool.max-idle:10}")
    private int maxIdle;

    @Value("${spring.redis.lettuce.pool.min-idle:1}")
    private int minIdle;

    /**
     * 往IOC容器中注册RedisConnectionFactory对象
     * <p>
     * 主要是针对redis集群的配置，暂时不实现，只实现单机版
     *
     * @return LettuceConnectionFactory
     */
    @Bean(destroyMethod = "destroy")
    @Conditional(value = {RedisStandaloneCondition.class})
    @Scope(BeanDefinition.SCOPE_PROTOTYPE)
    public LettuceConnectionFactory getConnectionFactory() {
        // 单机配置
        RedisStandaloneConfiguration redisStandaloneConfiguration = new RedisStandaloneConfiguration();
        redisStandaloneConfiguration.setHostName(host);
        redisStandaloneConfiguration.setDatabase(database);
        redisStandaloneConfiguration.setPort(port);
        redisStandaloneConfiguration.setPassword(RedisPassword.of(password));

        // 连接池配置
        GenericObjectPoolConfig poolConfig = new GenericObjectPoolConfig();
        poolConfig.setMaxIdle(maxIdle);
        poolConfig.setMinIdle(minIdle);
        poolConfig.setMaxTotal(maxActive);
        poolConfig.setMaxWaitMillis(maxWait);

        // 实例化luttuce连接池配置对象
        LettuceClientConfiguration standaloneClientConfig = LettucePoolingClientConfiguration.builder()
                .commandTimeout(Duration.ofMillis(timeout)).poolConfig(poolConfig).build();
        return new LettuceConnectionFactory(redisStandaloneConfiguration,
                standaloneClientConfig);
    }
}