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

package io.sermant.discovery.service.lb.stats;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import io.sermant.core.plugin.config.PluginConfigManager;
import io.sermant.discovery.config.LbConfig;
import io.sermant.discovery.entity.ServiceInstance;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * Service metrics data, manage all instance data
 *
 * @author zhouss
 * @since 2022-09-29
 */
public class ServiceStats {
    private final LoadingCache<ServiceInstance, InstanceStats> instanceStatsCache;
    private final String serviceName;

    /**
     * Constructor
     *
     * @param serviceName Service name
     */
    public ServiceStats(String serviceName) {
        this.serviceName = serviceName;
        instanceStatsCache = CacheBuilder.newBuilder()
                .expireAfterWrite(PluginConfigManager.getPluginConfig(LbConfig.class).getStatsCacheExpireTime(),
                        TimeUnit.MINUTES)
                .build(new CacheLoader<ServiceInstance, InstanceStats>() {
                    @Override
                    public InstanceStats load(ServiceInstance key) {
                        return createStats();
                    }
                });
    }

    /**
     * Get the status statist
     *
     * @param serviceInstance Instance
     * @return InstanceStats
     */
    public InstanceStats getStats(ServiceInstance serviceInstance) {
        try {
            return instanceStatsCache.get(serviceInstance);
        } catch (ExecutionException e) {
            InstanceStats stats = createStats();
            instanceStatsCache.asMap().putIfAbsent(serviceInstance, stats);
            return stats;
        }
    }

    /**
     * Clean up all data
     */
    public void cleanUp() {
        instanceStatsCache.cleanUp();
    }

    public String getServiceName() {
        return serviceName;
    }

    private InstanceStats createStats() {
        return new InstanceStats();
    }
}
