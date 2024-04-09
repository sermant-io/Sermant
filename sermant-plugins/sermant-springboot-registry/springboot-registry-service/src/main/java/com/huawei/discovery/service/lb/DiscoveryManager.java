/*
 * Copyright (C) 2022-2024 Sermant Authors. All rights reserved.
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
 * Load Balancer Manager
 *
 * @author zhouss
 * @since 2022-09-26
 */
public enum DiscoveryManager {
    /**
     * Singleton
     */
    INSTANCE;

    private static final Logger LOGGER = LoggerFactory.getLogger();

    /**
     * Lb type cache key: LB type value: class type
     */
    private final Map<String, Class<? extends AbstractLoadbalancer>> lbCache = new HashMap<>();

    /**
     * Each service's own load balancing cache is managed separately to prevent stateful load balancing instances from
     * affecting each other
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
     * Startup method
     */
    public void start() {
        initServiceDiscoveryClient();
        loadLb();
        loadFilter();
        loadListen();
        cacheManager = new InstanceCacheManager(serviceDiscoveryClient, instanceListenable);
    }

    /**
     * register
     *
     * @param serviceInstance Register an instance
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
     * Stop related services
     *
     * @throws IOException Stop failed throw
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
        if (this.cacheManager != null) {
            this.cacheManager.stop();
        }
    }

    /**
     * Select an instance based on the cache and load balancing rules
     *
     * @param serviceName Service name
     * @return ServiceInstance
     */
    public Optional<ServiceInstance> choose(String serviceName) {
        return choose(serviceName, getLoadbalancer(serviceName));
    }

    /**
     * Select an instance and support custom LB
     *
     * @param serviceName Service name
     * @param customLb Load balancing
     * @return ServiceInstance
     */
    public Optional<ServiceInstance> choose(String serviceName, Loadbalancer customLb) {
        return choose(serviceName, customLb, null);
    }

    /**
     * Select an instance to support custom filters
     *
     * @param serviceName Service name
     * @param customLb Load balancing
     * @param customFilter Custom filters
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
            LOGGER.info(String.format(Locale.ENGLISH,
                    "Use the loadbalancer [%s], if the lb does not support, it will replace by RoundRobin",
                    lbConfig.getLbType()));
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
