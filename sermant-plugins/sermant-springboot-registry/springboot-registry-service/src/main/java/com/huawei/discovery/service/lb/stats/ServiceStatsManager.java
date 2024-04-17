/*
 * Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huawei.discovery.service.lb.stats;

import com.huawei.discovery.config.LbConfig;
import com.huawei.discovery.entity.ServiceInstance;

import com.huaweicloud.sermant.core.plugin.config.PluginConfigManager;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalListener;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * The service statistics manager mainly records the metric data of the corresponding service
 *
 * @author zhouss
 * @since 2022-09-29
 */
public enum ServiceStatsManager {
    /**
     * Singleton
     */
    INSTANCE;

    private final LoadingCache<String, ServiceStats> serverStatsCache;

    ServiceStatsManager() {
        final LbConfig lbConfig = PluginConfigManager.getPluginConfig(LbConfig.class);
        serverStatsCache = CacheBuilder.newBuilder()
                .expireAfterWrite(lbConfig.getStatsCacheExpireTime(), TimeUnit.MINUTES)
                .concurrencyLevel(lbConfig.getCacheConcurrencyLevel())
                .removalListener(
                        (RemovalListener<String, ServiceStats>) notification -> notification.getValue().cleanUp())
                .build(new CacheLoader<String, ServiceStats>() {
                    @Override
                    public ServiceStats load(String serviceName) {
                        return new ServiceStats(serviceName);
                    }
                });
    }

    /**
     * Get service statistics
     *
     * @param serviceName Service name
     * @return ServiceStats
     */
    public ServiceStats getServiceStats(String serviceName) {
        try {
            return serverStatsCache.get(serviceName);
        } catch (ExecutionException e) {
            // 手动创建
            final ServiceStats serviceStats = new ServiceStats(serviceName);
            serverStatsCache.put(serviceName, serviceStats);
            return serviceStats;
        }
    }

    /**
     * Obtain the metric statistics of the instance
     *
     * @param serviceInstance Instance
     * @return Obtain the status data of the instance
     */
    public InstanceStats getInstanceStats(ServiceInstance serviceInstance) {
        final ServiceStats serviceStats = getServiceStats(serviceInstance.getServiceName());
        return serviceStats.getStats(serviceInstance);
    }
}
