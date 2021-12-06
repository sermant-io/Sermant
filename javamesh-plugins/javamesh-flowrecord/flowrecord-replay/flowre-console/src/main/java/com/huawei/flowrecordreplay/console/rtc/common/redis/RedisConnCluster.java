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

package com.huawei.flowrecordreplay.console.rtc.common.redis;

import io.lettuce.core.ClientOptions;
import io.lettuce.core.SocketOptions;
import io.lettuce.core.cluster.ClusterClientOptions;
import io.lettuce.core.cluster.ClusterTopologyRefreshOptions;
import io.lettuce.core.resource.ClientResources;
import io.lettuce.core.resource.DefaultClientResources;
import io.lettuce.core.resource.DnsResolvers;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.core.env.MapPropertySource;
import org.springframework.data.redis.connection.RedisClusterConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettucePoolingClientConfiguration;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * redis集群连接类
 *
 * @author hanpeng
 * @since 2021-04-07
 */
@Configuration
public class RedisConnCluster {
    /**
     * 最大等待时间，单位ms
     */
    private static final long MAX_WAIT_MS = 10000L;
    /**
     * 逐出扫描的时间间隔(毫秒) 如果为负数,则不运行逐出线程, 默认-1
     */
    private static final long EVICTION_RUNS_MS = 1000L;
    /**
     * 自适应刷新超时时间
     */
    private static final long REFRESH_TRIGGERS_TIMEOUT = 30L;
    /**
     * 开启周期刷新
     */
    private static final long PERIODIC_REFRESH = 20L;
    @Value("${spring.redis.password:}")
    private String password;

    @Value("${spring.redis.cluster.nodes}")
    private String nodes;

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

    @Value("${spring.redis.cluster.max-redirects}")
    private int redirects;

    /**
     * 往IOC容器中注册RedisConnectionFactory对象
     * <p>
     * 主要是针对redis集群的配置，暂时不实现，只实现单机版
     *
     * @return LettuceConnectionFactory
     */
    @Bean(destroyMethod = "destroy")
    @Conditional(value = {RedisClusterCondition.class})
    @Scope(BeanDefinition.SCOPE_PROTOTYPE)
    public LettuceConnectionFactory getConnectionFactory() {
        // 集群配置
        Map<String, Object> source = new HashMap<>();
        source.put("spring.redis.cluster.nodes", nodes);
        source.put("spring.redis.timeout", timeout);
        source.put("spring.redis.database", database);
        source.put("spring.redis.password", password);
        RedisClusterConfiguration redisClusterConfiguration = new RedisClusterConfiguration(
                new MapPropertySource("RedisClusterConfiguration", source));
        redisClusterConfiguration.setMaxRedirects(redirects);

        // 连接池配置
        GenericObjectPoolConfig genericObjectPoolConfig = new GenericObjectPoolConfig();
        genericObjectPoolConfig.setMaxIdle(maxIdle);
        genericObjectPoolConfig.setMinIdle(minIdle);
        genericObjectPoolConfig.setMaxTotal(maxActive);
        genericObjectPoolConfig.setMaxWaitMillis(maxWait);
        genericObjectPoolConfig.setMaxWaitMillis(MAX_WAIT_MS);
        genericObjectPoolConfig.setTimeBetweenEvictionRunsMillis(EVICTION_RUNS_MS);

        // 开启自适应集群拓扑刷新和周期拓扑刷新
        ClusterTopologyRefreshOptions clusterTopologyRefreshOptions = ClusterTopologyRefreshOptions.builder()

                // 开启全部自适应刷新
                .enableAllAdaptiveRefreshTriggers()

                // 自适应刷新超时时间(默认30秒)
                .adaptiveRefreshTriggersTimeout(Duration.ofSeconds(REFRESH_TRIGGERS_TIMEOUT))

                // 开周期刷新
                .enablePeriodicRefresh(Duration.ofSeconds(PERIODIC_REFRESH))
                .build();
        ClientResources clientResources = DefaultClientResources
                .builder()
                .dnsResolver(DnsResolvers.JVM_DEFAULT).build();
        final SocketOptions socketOptions = SocketOptions.builder()
                .connectTimeout(Duration.ofMillis(timeout))
                .build();
        final ClientOptions clientOptions = ClusterClientOptions.builder()
                .socketOptions(socketOptions)
                .autoReconnect(true)
                .topologyRefreshOptions(clusterTopologyRefreshOptions)
                .build();
        LettuceClientConfiguration clientConfig = LettucePoolingClientConfiguration.builder()
                .poolConfig(genericObjectPoolConfig)
                .clientOptions(clientOptions)
                .clientResources(clientResources)
                .commandTimeout(Duration.ofMillis(timeout))
                .build();
        return new LettuceConnectionFactory(redisClusterConfiguration,
                clientConfig);
    }
}