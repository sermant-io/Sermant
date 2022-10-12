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

package com.huawei.discovery.service.lb.cache;

import com.huawei.discovery.config.LbConfig;
import com.huawei.discovery.entity.ServiceInstance;
import com.huawei.discovery.service.ex.QueryInstanceException;
import com.huawei.discovery.service.lb.LbConstants;
import com.huawei.discovery.service.lb.discovery.InstanceChangeListener;
import com.huawei.discovery.service.lb.discovery.InstanceListenable;
import com.huawei.discovery.service.lb.discovery.ServiceDiscoveryClient;

import com.huaweicloud.sermant.core.common.LoggerFactory;
import com.huaweicloud.sermant.core.plugin.config.PluginConfigManager;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalListener;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * 实例缓存
 *
 * @author zhouss
 * @since 2022-09-26
 */
public class InstanceCacheManager {
    private static final Logger LOGGER = LoggerFactory.getLogger();

    private final InstanceChangeListener listener = new InstanceUpdater();

    private final Map<String, InstanceCache> oldInstancesCache = new ConcurrentHashMap<>();

    private final LoadingCache<String, InstanceCache> cache;

    private final ServiceDiscoveryClient discoveryClient;

    private final InstanceListenable instanceListenable;

    /**
     * 构造器
     *
     * @param discoveryClient 查询客户端
     * @param instanceListenable 监听
     */
    public InstanceCacheManager(ServiceDiscoveryClient discoveryClient,
            InstanceListenable instanceListenable) {
        this.discoveryClient = discoveryClient;
        this.instanceListenable = instanceListenable;
        final LbConfig lbConfig = PluginConfigManager.getPluginConfig(LbConfig.class);
        this.cache = CacheBuilder.newBuilder()
                .refreshAfterWrite(lbConfig.getInstanceCacheExpireTime(), TimeUnit.MINUTES)
                .expireAfterWrite(lbConfig.getInstanceCacheExpireTime(), TimeUnit.MINUTES)
                .removalListener((RemovalListener<String, InstanceCache>) notification -> {
                    if (lbConfig.isKeepOldInstancesWhenErr()) {
                        oldInstancesCache.put(notification.getKey(), notification.getValue());
                    }
                })
                .concurrencyLevel(lbConfig.getCacheConcurrencyLevel())
                .build(new CacheLoader<String, InstanceCache>() {
                    @Override
                    public InstanceCache load(String serviceName) {
                        return createCache(serviceName);
                    }
                });
    }

    /**
     * 获取实例列表
     *
     * @param serviceName 服务名
     * @return 实例列表
     */
    public List<ServiceInstance> getInstances(String serviceName) {
        final List<ServiceInstance> instances = getInstanceCache(serviceName).getInstances();
        if (instances == null || instances.isEmpty()) {
            return tryUpdateInstances(serviceName);
        }
        return new ArrayList<>(instances);
    }

    private InstanceCache getInstanceCache(String serviceName) {
        try {
            tryAddListen(serviceName);
            return cache.get(serviceName);
        } catch (ExecutionException e) {
            return createCache(serviceName);
        }
    }

    private void tryAddListen(String serviceName) {
        if (instanceListenable != null) {
            instanceListenable.tryAdd(serviceName, listener);
        }
    }

    private List<ServiceInstance> tryUpdateInstances(String serviceName) {
        cache.refresh(serviceName);
        return new ArrayList<>(getInstanceCache(serviceName).getInstances());
    }

    private InstanceCache createCache(String serviceName) {
        Collection<ServiceInstance> instances = null;
        try {
            instances = discoveryClient.getInstances(serviceName);
        } catch (QueryInstanceException ex) {
            // 注册中心可能出现问题, 此时使用旧实例替换
            final InstanceCache instanceCache = oldInstancesCache.get(serviceName);
            if (instanceCache != null) {
                return instanceCache;
            }
        }
        if (instances != null && !instances.isEmpty()) {
            return new InstanceCache(serviceName, new ArrayList<>(instances));
        }
        return new InstanceCache(serviceName, new ArrayList<>());
    }

    /**
     * 实例更新器, 使数据更加实时
     *
     * @since 2022-10-12
     */
    class InstanceUpdater implements InstanceChangeListener {
        @Override
        public void notify(EventType eventType, ServiceInstance serviceInstance) {
            final String serviceName = serviceInstance.getServiceName();
            InstanceCache instanceCache = getInstanceCache(serviceName);
            List<ServiceInstance> instances = instanceCache.getInstances();
            if (instances.isEmpty()) {
                // 尝试重试新新实例
                instanceCache = createCache(serviceName);
                instances = instanceCache.getInstances();
            }
            if (eventType == EventType.DELETED) {
                removeInstance(instances, serviceInstance);
            } else if (eventType == EventType.ADDED) {
                if (!isContains(instances, serviceInstance)) {
                    instances.add(serviceInstance);
                }
            } else {
                removeInstance(instances, serviceInstance);
                instances.add(serviceInstance);
            }
            printLog(eventType, serviceInstance);
            cache.put(serviceName, instanceCache);
        }

        private void printLog(EventType eventType, ServiceInstance serviceInstance) {
            final Map<String, String> metadata = serviceInstance.getMetadata();
            if (metadata == null) {
                return;
            }
            final String id = metadata.get(LbConstants.SERMANT_DISCOVERY);
            if (id != null) {
                LOGGER.info(String.format(Locale.ENGLISH, "Service instance [%s] has been [%s]!",
                        id, eventType.name()));
            }
        }

        private boolean isContains(List<ServiceInstance> instances, ServiceInstance serviceInstance) {
            return instances.stream().anyMatch(instance -> this.isSameInstance(instance, serviceInstance));
        }

        private void removeInstance(List<ServiceInstance> instances, ServiceInstance serviceInstance) {
            instances.removeIf(instance -> this.isSameInstance(instance, serviceInstance));
        }

        private boolean isSameInstance(ServiceInstance source, ServiceInstance target) {
            return (source.getIp().equals(target.getIp()) || source.getHost().equals(target.getHost()))
                    && source.getPort() == target.getPort();
        }
    }
}
