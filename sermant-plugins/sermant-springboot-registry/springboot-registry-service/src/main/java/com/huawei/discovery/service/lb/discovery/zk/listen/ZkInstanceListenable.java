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

package com.huawei.discovery.service.lb.discovery.zk.listen;

import com.huawei.discovery.config.LbConfig;
import com.huawei.discovery.service.lb.discovery.InstanceChangeListener;
import com.huawei.discovery.service.lb.discovery.InstanceChangeListener.EventType;
import com.huawei.discovery.service.lb.discovery.InstanceListenable;
import com.huawei.discovery.service.lb.discovery.zk.ZkDiscoveryClient;
import com.huawei.discovery.service.lb.discovery.zk.ZkDiscoveryClient.ZkInstanceSerializer;
import com.huawei.discovery.service.lb.discovery.zk.ZkInstanceHelper;

import com.huaweicloud.sermant.core.common.LoggerFactory;
import com.huaweicloud.sermant.core.plugin.config.PluginConfigManager;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCache.StartMode;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent.Type;
import org.apache.curator.retry.RetryForever;
import org.apache.curator.x.discovery.ServiceInstance;
import org.springframework.cloud.zookeeper.discovery.ZookeeperInstance;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * zk监听实现
 *
 * @author zhouss
 * @since 2022-10-12
 */
public class ZkInstanceListenable implements InstanceListenable {
    private static final Logger LOGGER = LoggerFactory.getLogger();

    private final Map<String, InstanceChangeListener> listenerCache = new ConcurrentHashMap<>();

    private final Map<String, PathChildrenCache> pathChildrenCacheMap = new ConcurrentHashMap<>();

    private final ZkInstanceSerializer<ZookeeperInstance> serializer =
            new ZkInstanceSerializer<>(ZookeeperInstance.class);

    private final LbConfig lbConfig;

    private final Predicate<ServiceInstance<ZookeeperInstance>> predicate;

    private CuratorFramework curatorFramework;

    /**
     * 构造器
     */
    public ZkInstanceListenable() {
        this.lbConfig = PluginConfigManager.getPluginConfig(LbConfig.class);
        this.predicate = ZkInstanceHelper.predicate(lbConfig.isOnlyCurRegisterInstances());
    }

    @Override
    public void init() {
        this.curatorFramework = buildClient();
        this.curatorFramework.start();
    }

    private CuratorFramework buildClient() {
        return CuratorFrameworkFactory.newClient(lbConfig.getRegistryAddress(), lbConfig.getReadTimeoutMs(),
                lbConfig.getConnectionTimeoutMs(), new RetryForever(lbConfig.getRetryIntervalMs()));
    }

    @Override
    public void tryAdd(String serviceName, InstanceChangeListener listener) {
        if (listenerCache.get(serviceName) != null) {
            return;
        }
        listenerCache.put(serviceName, listener);
        if (pathChildrenCacheMap.get(serviceName) != null) {
            return;
        }
        createCache(serviceName, listener).ifPresent(pathChildrenCache ->
                pathChildrenCacheMap.put(serviceName, pathChildrenCache));
    }

    private Optional<PathChildrenCache> createCache(String serviceName, InstanceChangeListener listener) {
        final PathChildrenCache pathChildrenCache = new PathChildrenCache(curatorFramework, buildPath(serviceName), true);
        try {
            pathChildrenCache.start(StartMode.POST_INITIALIZED_EVENT);
        } catch (Exception exception) {
            LOGGER.log(Level.WARNING, "Can not start path cache!", exception);
            return Optional.empty();
        }
        pathChildrenCache.getListenable().addListener((client, event) -> {
            final Type type = event.getType();
            if (!isTargetEvent(type)) {
                return;
            }
            final Optional<com.huawei.discovery.entity.ServiceInstance> deserialize = deserialize(event.getData());
            deserialize.ifPresent(serviceInstance -> listener.notify(formatEventType(type), serviceInstance));
        });
        return Optional.of(pathChildrenCache);
    }

    private EventType formatEventType(Type type) {
        if (type == Type.CHILD_REMOVED) {
            return EventType.DELETED;
        } else if (type == Type.CHILD_UPDATED) {
            return EventType.UPDATED;
        } else {
            return EventType.ADDED;
        }
    }

    private Optional<com.huawei.discovery.entity.ServiceInstance> deserialize(ChildData childData) {
        final ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(ZkDiscoveryClient.class.getClassLoader());
            final ServiceInstance<ZookeeperInstance> serviceInstance = serializer.deserialize(childData.getData());
            if (!predicate.test(serviceInstance)) {
                return Optional.empty();
            }
            return Optional.of(ZkInstanceHelper.convert2Instance(serviceInstance));
        } catch (Exception exception) {
            LOGGER.warning(String.format("Can not deserialize instance, may be it is not valid, path is [%s]",
                    childData.getPath()));
        } finally {
            Thread.currentThread().setContextClassLoader(contextClassLoader);
        }
        return Optional.empty();
    }

    private boolean isTargetEvent(Type type) {
        return type == Type.CHILD_ADDED || type == Type.CHILD_REMOVED || type == Type.CHILD_UPDATED;
    }

    private String buildPath(String serviceName) {
        return lbConfig.getZkBasePath() + "/" + serviceName;
    }

    @Override
    public void close() {
        pathChildrenCacheMap.values().forEach(pathChildrenCache -> {
            try {
                pathChildrenCache.close();
            } catch (IOException ex) {
                LOGGER.log(Level.WARNING, "Can not close zk path children cache!", ex);
            }
        });
        listenerCache.clear();
    }

    @Override
    public String name() {
        return "Zookeeper";
    }
}
