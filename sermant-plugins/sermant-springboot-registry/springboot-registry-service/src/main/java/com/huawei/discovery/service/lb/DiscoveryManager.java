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

package com.huawei.discovery.service.lb;

import com.huawei.discovery.config.LbConfig;
import com.huawei.discovery.entity.ServiceInstance;
import com.huawei.discovery.service.lb.cache.InstanceCacheManager;
import com.huawei.discovery.service.lb.discovery.InstanceListenable;
import com.huawei.discovery.service.lb.discovery.ServiceDiscoveryClient;
import com.huawei.discovery.service.lb.filter.InstanceFilter;
import com.huawei.discovery.service.lb.rule.AbstractLoadbalancer;
import com.huawei.discovery.service.lb.rule.Loadbalancer;
import com.huawei.discovery.service.lb.rule.RoundRobinLoadbalancer;

import com.huaweicloud.sermant.core.common.LoggerFactory;
import com.huaweicloud.sermant.core.plugin.config.PluginConfigManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 负载均衡管理器
 *
 * @author zhouss
 * @since 2022-09-26
 */
public enum DiscoveryManager {
    /**
     * 单例
     */
    INSTANCE;

    private static final Logger LOGGER = LoggerFactory.getLogger();

    /**
     * lb类型缓存
     * key: lb类型
     * value: class类型
     */
    private final Map<String, Class<? extends AbstractLoadbalancer>> lbCache = new HashMap<>();

    /**
     * 各个服务自身的负载均衡缓存, 分开管理, 防止有状态负载均衡实例相互影响
     */
    private final Map<String, AbstractLoadbalancer> serviceLbCache = new ConcurrentHashMap<>();

    private final List<InstanceFilter> filters = new ArrayList<>();

    private final AtomicBoolean isStarted = new AtomicBoolean();

    private final LbConfig lbConfig;

    private ServiceDiscoveryClient serviceDiscoveryClient;

    private InstanceListenable instanceListenable;

    private InstanceCacheManager cacheManager;

    DiscoveryManager() {
        lbConfig = PluginConfigManager.getPluginConfig(LbConfig.class);
    }

    private void initServiceDiscoveryClient() {
        final String registryCenterType = lbConfig.getRegistryCenterType();
        for (ServiceDiscoveryClient discoveryClient : ServiceLoader.load(ServiceDiscoveryClient.class, this.getClass()
                .getClassLoader())) {
            if (discoveryClient.name().equalsIgnoreCase(lbConfig.getRegistryCenterType())) {
                this.serviceDiscoveryClient = discoveryClient;
                break;
            }
        }
        if (this.serviceDiscoveryClient == null) {
            throw new IllegalStateException("Can not support register center type: " + registryCenterType);
        }
        this.serviceDiscoveryClient.init();
    }

    private void loadLb() {
        for (AbstractLoadbalancer loadbalancer : ServiceLoader.load(AbstractLoadbalancer.class, this.getClass()
                .getClassLoader())) {
            lbCache.put(loadbalancer.lbType(), loadbalancer.getClass());
        }
    }

    private void loadListen() {
        for (InstanceListenable listenable : ServiceLoader.load(InstanceListenable.class, this.getClass()
                .getClassLoader())) {
            if (listenable.name().equalsIgnoreCase(lbConfig.getRegistryCenterType())) {
                this.instanceListenable = listenable;
                break;
            }
        }
        if (this.instanceListenable != null) {
            this.instanceListenable.init();
        }
    }

    /**
     * 启动方法
     */
    public void start() {
        initServiceDiscoveryClient();
        loadLb();
        loadFilter();
        loadListen();
        cacheManager = new InstanceCacheManager(serviceDiscoveryClient, instanceListenable);
    }

    /**
     * 注册
     *
     * @param serviceInstance 注册实例
     */
    public void registry(ServiceInstance serviceInstance) {
        checkStats();
        if (serviceDiscoveryClient.registry(serviceInstance)) {
            LOGGER.info("Registry instance to registry center success!");
        }
    }

    private void loadFilter() {
        for (InstanceFilter filter : ServiceLoader.load(InstanceFilter.class, this.getClass()
                .getClassLoader())) {
            this.filters.add(filter);
        }
    }

    /**
     * 停止相关服务
     *
     * @throws IOException 停止失败抛出
     */
    public void stop() throws IOException {
        lbCache.clear();
        serviceLbCache.clear();
        if (serviceDiscoveryClient.unRegistry()) {
            LOGGER.info("Cur instance has been un registry from registry center success!");
        }
        serviceDiscoveryClient.close();
        if (instanceListenable != null) {
            instanceListenable.close();
        }
    }

    /**
     * 基于缓存以及负载均衡规则选择实例
     *
     * @param serviceName 服务名
     * @return ServiceInstance
     */
    public Optional<ServiceInstance> choose(String serviceName) {
        return choose(serviceName, getLoadbalancer(serviceName));
    }

    /**
     * 选择实例, 支持自定义lb
     *
     * @param serviceName 服务名
     * @param customLb 负载均衡
     * @return ServiceInstance
     */
    public Optional<ServiceInstance> choose(String serviceName, Loadbalancer customLb) {
        return choose(serviceName, customLb, null);
    }

    /**
     * 选择实例, 支持自定义过滤器
     *
     * @param serviceName 服务名
     * @param customLb 负载均衡
     * @param customFilter 自定义过滤器
     * @return ServiceInstance
     */
    public Optional<ServiceInstance> choose(String serviceName, Loadbalancer customLb, InstanceFilter customFilter) {
        checkStats();
        List<ServiceInstance> instances = cacheManager.getInstances(serviceName);
        for (InstanceFilter filter : filters) {
            instances = filter.filter(serviceName, instances);
        }
        if (customFilter != null) {
            instances = customFilter.filter(serviceName, instances);
        }
        return customLb.choose(serviceName, instances);
    }

    private void checkStats() {
        if (isStarted.compareAndSet(false, true)) {
            this.start();
        }
    }

    private Loadbalancer getLoadbalancer(String serviceName) {
        checkStats();
        return serviceLbCache.computeIfAbsent(serviceName, curService -> {
            final Class<? extends AbstractLoadbalancer> lbClazz = lbCache
                    .getOrDefault(lbConfig.getLbType(), RoundRobinLoadbalancer.class);
            return createLb(lbClazz).orElse(null);
        });
    }

    private Optional<AbstractLoadbalancer> createLb(Class<? extends AbstractLoadbalancer> clazz) {
        try {
            return Optional.of(clazz.newInstance());
        } catch (InstantiationException e) {
            LOGGER.log(Level.WARNING, String.format(Locale.ENGLISH,
                    "Can not create instance for clazz [%s] with no params constructor!", clazz.getName()),
                    e);
        } catch (IllegalAccessException e) {
            LOGGER.log(Level.WARNING, String.format(Locale.ENGLISH,
                    "Can not access no params constructor for clazz [%s]!", clazz.getName()), e);
        }
        return Optional.empty();
    }
}
