/*
 * Copyright (C) 2022-2024 Sermant Authors. All rights reserved.
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

import java.util.Collection;
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
 * Gracefully rolled off the line
 *
 * @author zhouss
 * @since 2022-05-23
 */
public class GraceShutDownManager {
    private static final Logger LOGGER = LoggerFactory.getLogger();

    /**
     * The maximum number of endpoints that can be disabled for caching
     */
    private static final int MAX_SHUTDOWN_ENDPOINT_CACHE = 2000;

    private static final ScheduledThreadPoolExecutor CLEAN_UP_TASK = new ScheduledThreadPoolExecutor(1,
            new ThreadFactoryUtils("ENDPOINT_CLEAN_UP_TASK"));

    /**
     * Number of Requests: Statistics on the number of requests after they are marked as closed
     */
    private final AtomicInteger requestCount = new AtomicInteger();

    /**
     * Whether the current instance is marked as down
     */
    private volatile boolean isShutDown = false;

    /**
     * The load balancer is used to cache the balancer, and when the service is notified to go offline, the loadBalancer
     * is used to pull the latest service instance.
     * key: downstream service name
     * value: loadbalancer in a timely manner
     */
    private final Map<String, Object> loadBalancerCache = new ConcurrentHashMap<>();

    /**
     * Spring Cache Manager, which is used for load balancing to get the CachingServiceInstanceListSupplier cache and
     * flush the cache based on the evict method
     */
    private Object loadBalancerCacheManager;

    /**
     * Cache the downstream IP that is about to be shut down, key: endpoint value: The closing timestamp of the closure
     * being marked
     */
    private final Map<String, Long> markShutDownEndpoints = new ConcurrentHashMap<>();

    /**
     * Register bean
     */
    private Object registration;

    GraceShutDownManager() {
        final long endpointExpiredTime = PluginConfigManager.getPluginConfig(GraceConfig.class)
                .getEndpointExpiredTime();
        CLEAN_UP_TASK.scheduleAtFixedRate(this::cleanUp, 0L, endpointExpiredTime, TimeUnit.SECONDS);
    }

    /**
     * Increase the number of requests and return them together with the request form
     */
    public void increaseRequestCount() {
        requestCount.incrementAndGet();
    }

    /**
     * Request quantity minus one and return it
     */
    public void decreaseRequestCount() {
        requestCount.decrementAndGet();
    }

    /**
     * Get the number of requests
     *
     * @return Number of requests
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
     * Check whether the endpoint has been marked offline
     *
     * @param endpoint Downstream request address
     * @return true, It has been marked offline
     */
    public boolean isMarkedOffline(String endpoint) {
        return markShutDownEndpoints.containsKey(endpoint);
    }

    /**
     * Add the downstream IP address that you want to shut down
     *
     * @param endpoints Address，host:port
     */
    public void addShutdownEndpoints(Collection<String> endpoints) {
        if (endpoints == null || endpoints.isEmpty()) {
            return;
        }
        endpoints.forEach(this::addShutdownEndpoint);
    }

    private void addShutdownEndpoint(String endpoint) {
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
