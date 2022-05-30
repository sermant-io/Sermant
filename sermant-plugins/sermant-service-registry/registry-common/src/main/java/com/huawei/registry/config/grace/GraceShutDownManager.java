/*
 * Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.huawei.registry.config.grace;

import com.huawei.registry.config.ConfigConstants;
import com.huawei.registry.config.GraceConfig;

import com.huaweicloud.sermant.core.common.LoggerFactory;
import com.huaweicloud.sermant.core.plugin.config.PluginConfigManager;
import com.huaweicloud.sermant.core.utils.ThreadFactoryUtils;

import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

/**
 * 优雅下线
 *
 * @author zhouss
 * @since 2022-05-23
 */
public class GraceShutDownManager {
    private static final Logger LOGGER = LoggerFactory.getLogger();

    /**
     * 最大缓存关闭endpoint数量
     */
    private static final int MAX_SHUTDOWN_ENDPOINT_CACHE = 1000;

    private static final ScheduledThreadPoolExecutor CLEAN_UP_TASK = new ScheduledThreadPoolExecutor(1,
            new ThreadFactoryUtils("ENDPOINT_CLEAN_UP_TASK"));

    /**
     * 请求数, 统计被标记关闭后的请求数量
     */
    private final AtomicInteger requestCount = new AtomicInteger();

    /**
     * 当前实例是否被标记为关闭状态
     */
    private volatile boolean isShutDown = false;

    /**
     * 负载均衡缓存, 用于缓存balancer, 在服务被通知下线时, 使用该loadBalancer及时拉取最新服务实例 key : 下游服务名 value: loadbalancer
     */
    private final Map<String, Object> loadBalancerCache = new ConcurrentHashMap<>();

    /**
     * Spring缓存管理器, 用于负载均衡获取CachingServiceInstanceListSupplier缓存, 并刷新该缓存基于evict方法
     */
    private Object loadBalancerCacheManager;

    /**
     * 缓存即将要关闭的下游IP key: endpoint value: 关闭被标记的关闭时间戳
     */
    private final Map<String, Long> markShutDownEndpoints = new ConcurrentHashMap<>();

    /**
     * 注册bean
     */
    private Object registration;

    GraceShutDownManager() {
        final long endpointExpiredTime = PluginConfigManager.getPluginConfig(GraceConfig.class)
                .getEndpointExpiredTime();
        CLEAN_UP_TASK.scheduleAtFixedRate(this::cleanUp, 0L, endpointExpiredTime, TimeUnit.SECONDS);
    }

    /**
     * +1增加请求数并返回
     */
    public void increaseRequestCount() {
        requestCount.incrementAndGet();
    }

    /**
     * -1请求数并返回
     */
    public void decreaseRequestCount() {
        requestCount.decrementAndGet();
    }

    /**
     * 获取请求数
     *
     * @return 请求数
     */
    public int getRequestCount() {
        return requestCount.get();
    }

    public boolean isShutDown() {
        return isShutDown;
    }

    public void setShutDown(boolean shutDown) {
        isShutDown = shutDown;
    }

    public Map<String, Object> getLoadBalancerCache() {
        return loadBalancerCache;
    }

    public Object getLoadBalancerCacheManager() {
        return loadBalancerCacheManager;
    }

    public void setLoadBalancerCacheManager(Object loadBalancerCacheManager) {
        this.loadBalancerCacheManager = loadBalancerCacheManager;
    }

    public Object getRegistration() {
        return registration;
    }

    public void setRegistration(Object registration) {
        this.registration = registration;
    }

    /**
     * 当前endpoint是否已被标记下线
     *
     * @param endpoint 下游请求地址
     * @return true 已被标记下线
     */
    public boolean isMarkedOffline(String endpoint) {
        return markShutDownEndpoints.containsKey(endpoint);
    }

    /**
     * 添加要关闭的下游ip地址
     *
     * @param endpoint 地址，host:port
     */
    public void addShutdownEndpoint(String endpoint) {
        if (markShutDownEndpoints.size() < MAX_SHUTDOWN_ENDPOINT_CACHE) {
            markShutDownEndpoints.put(endpoint, System.currentTimeMillis());
            LOGGER.fine(String.format(Locale.ENGLISH, "Marked endpoint [%s] will be shutdown!", endpoint));
        } else {
            cleanUp();
            LOGGER.warning(String.format(Locale.ENGLISH,
                "Exceed the max mark shutdown endpoints size! endpoint [%s]", endpoint));
        }
    }

    private void cleanUp() {
        final long expiredTime = PluginConfigManager.getPluginConfig(GraceConfig.class).getEndpointExpiredTime()
                * ConfigConstants.SEC_DELTA;
        final Iterator<Entry<String, Long>> iterator = markShutDownEndpoints.entrySet().iterator();
        final long currentTimeMillis = System.currentTimeMillis();
        while (iterator.hasNext()) {
            final Entry<String, Long> next = iterator.next();
            final Long invokeTime = next.getValue();
            if (currentTimeMillis - invokeTime >= expiredTime) {
                iterator.remove();
            }
        }
    }
}
