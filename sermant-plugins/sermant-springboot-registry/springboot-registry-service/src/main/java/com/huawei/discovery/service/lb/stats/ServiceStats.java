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

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * 服务指标数据, 管理所有实例数据
 *
 * @author zhouss
 * @since 2022-09-29
 */
public class ServiceStats {
    private final LoadingCache<ServiceInstance, InstanceStats> instanceStatsCache;
    private final String serviceName;

    /**
     * 构造器
     *
     * @param serviceName 服务名
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
     * 获取状态统计器
     *
     * @param serviceInstance 实例
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
     * 清理所有数据
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
