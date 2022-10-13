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
import com.huawei.discovery.factory.RealmServiceThreadFactory;
import com.huawei.discovery.service.ex.QueryInstanceException;
import com.huawei.discovery.service.lb.LbConstants;
import com.huawei.discovery.service.lb.discovery.InstanceChangeListener;
import com.huawei.discovery.service.lb.discovery.InstanceListenable;
import com.huawei.discovery.service.lb.discovery.ServiceDiscoveryClient;

import com.huaweicloud.sermant.core.common.LoggerFactory;
import com.huaweicloud.sermant.core.plugin.config.PluginConfigManager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledThreadPoolExecutor;
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

    private final Map<String, InstanceCache> instanceCaches = new ConcurrentHashMap<>();

    private final LbConfig lbConfig;

    private final ServiceDiscoveryClient discoveryClient;

    private final InstanceListenable instanceListenable;

    private ScheduledThreadPoolExecutor instanceUpdater;

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
        this.lbConfig = PluginConfigManager.getPluginConfig(LbConfig.class);
        initUpdater();
    }

    private void initUpdater() {
        final long instanceCacheExpireTime = lbConfig.getInstanceCacheExpireTime();
        if (instanceCacheExpireTime <= 0) {
            return;
        }
        checkParams(instanceCacheExpireTime);
        startUpdater();
    }

    private void checkParams(long instanceCacheExpireTime) {
        final long instanceRefreshInterval = lbConfig.getInstanceRefreshInterval();
        if (instanceCacheExpireTime < instanceRefreshInterval) {
            throw new IllegalArgumentException(String.format(Locale.ENGLISH,
                    "instanceCacheExpireTime(%s sec) must gt instanceRefreshInterval(%s sec)",
                    instanceCacheExpireTime, instanceRefreshInterval));
        }
        final long refreshTimerInterval = lbConfig.getRefreshTimerInterval();
        if (refreshTimerInterval <= 0) {
            throw new IllegalArgumentException(String.format(Locale.ENGLISH,
                    "Invalid refreshTimerInterval: %s", refreshTimerInterval));
        }
        if (instanceCacheExpireTime < refreshTimerInterval) {
            throw new IllegalArgumentException(String.format(Locale.ENGLISH,
                    "instanceCacheExpireTime(%s sec) must gt refreshTimerInterval(%s sec)",
                    instanceCacheExpireTime, refreshTimerInterval));
        }
        if (refreshTimerInterval > instanceRefreshInterval) {
            LOGGER.info(String.format(Locale.ENGLISH,
                    "refreshTimerInterval(%s) is gt instanceRefreshInterval(%s), set it to %s",
                    refreshTimerInterval, instanceRefreshInterval, instanceCaches));
            lbConfig.setRefreshTimerInterval(instanceRefreshInterval);
        }
    }

    private void startUpdater() {
        this.instanceUpdater = new ScheduledThreadPoolExecutor(1,
                new RealmServiceThreadFactory("springboot-registry-instance-update-thread"));
        this.instanceUpdater.scheduleAtFixedRate(new InstanceRefresher(
                lbConfig.getInstanceCacheExpireTime() * LbConstants.SEC_TO_MS,
                lbConfig.getInstanceRefreshInterval() * LbConstants.SEC_TO_MS), 0,
                lbConfig.getRefreshTimerInterval(), TimeUnit.SECONDS);
    }

    /**
     * 停止方法
     */
    public void stop() {
        if (this.instanceUpdater != null) {
            this.instanceUpdater.shutdown();
        }
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
            return Collections.emptyList();
        }
        return new ArrayList<>(instances);
    }

    private InstanceCache getInstanceCache(String serviceName) {
        tryAddListen(serviceName);
        return instanceCaches.getOrDefault(serviceName, createCache(serviceName));
    }

    private void tryAddListen(String serviceName) {
        if (instanceListenable != null) {
            instanceListenable.tryAdd(serviceName, listener);
        }
    }

    private InstanceCache createCache(String serviceName) {
        Collection<ServiceInstance> instances = null;
        try {
            instances = discoveryClient.getInstances(serviceName);
        } catch (QueryInstanceException ex) {
            // 注册中心可能出现问题, 返回空, 定时器将会更新实例
            final InstanceCache instanceCache = oldInstancesCache.get(serviceName);
            if (instanceCache != null) {
                instanceCache.setUpdateTimestamp(System.currentTimeMillis());
                return instanceCache;
            }
        }
        if (instances != null && !instances.isEmpty()) {
            return new InstanceCache(serviceName, new ArrayList<>(instances));
        }
        return new InstanceCache(serviceName, new ArrayList<>());
    }

    /**
     * 实例刷新器, 需定时执行
     *
     * @since 2022-10-13
     */
    class InstanceRefresher implements Runnable {
        private final long instanceCacheExpireTimeMs;
        private long instanceRefreshIntervalMs;

        InstanceRefresher(long instanceCacheExpireTimeMs, long instanceRefreshIntervalMs) {
            this.instanceCacheExpireTimeMs = instanceCacheExpireTimeMs;
            this.instanceRefreshIntervalMs = instanceRefreshIntervalMs;
            reCalculateRefreshIntervalMs();
        }

        private void reCalculateRefreshIntervalMs() {
            if (this.instanceRefreshIntervalMs > 0) {
                return;
            }
            if (this.instanceCacheExpireTimeMs >= LbConstants.MIN_GAP_MS_BEFORE_EXPIRE_MS) {
                this.instanceRefreshIntervalMs = this.instanceCacheExpireTimeMs - LbConstants.GAP_MS_BEFORE_EXPIRE_MS;
            } else {
                this.instanceRefreshIntervalMs =
                        (long) (this.instanceCacheExpireTimeMs * (1 - LbConstants.GAP_MS_BEFORE_EXPIRE_DELTA));
            }
        }

        @Override
        public void run() {
            final long currentTimeMillis = System.currentTimeMillis();
            final Map<String, InstanceCache> updateCaches = new HashMap<>();
            instanceCaches.values().forEach(instanceCache -> {
                final long createTimestamp = instanceCache.getUpdateTimestamp();
                if (currentTimeMillis - createTimestamp >= this.instanceRefreshIntervalMs) {
                    // 缓存过期, 刷新缓存
                    final String serviceName = instanceCache.getServiceName();
                    final InstanceCache cache = createCache(serviceName);
                    if (!cache.getInstances().isEmpty()) {
                        updateCaches.put(serviceName, cache);
                        oldInstancesCache.put(serviceName, cache);
                    } else {
                        // 注册中心可能有问题, 此时不刷新实例, 同时更新时间戳
                        instanceCache.setUpdateTimestamp(currentTimeMillis);
                    }
                }
            });
            instanceCaches.putAll(updateCaches);
        }
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
            instanceCaches.put(serviceName, instanceCache);
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
