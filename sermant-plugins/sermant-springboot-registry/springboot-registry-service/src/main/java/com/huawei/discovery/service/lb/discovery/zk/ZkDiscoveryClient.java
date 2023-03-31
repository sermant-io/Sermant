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

package com.huawei.discovery.service.lb.discovery.zk;

import com.huawei.discovery.config.LbConfig;
import com.huawei.discovery.entity.ServiceInstance;
import com.huawei.discovery.service.ex.QueryInstanceException;
import com.huawei.discovery.service.lb.LbConstants;
import com.huawei.discovery.service.lb.discovery.ServiceDiscoveryClient;

import com.huaweicloud.sermant.core.common.LoggerFactory;
import com.huaweicloud.sermant.core.plugin.config.PluginConfigManager;
import com.huaweicloud.sermant.core.plugin.service.PluginServiceManager;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.curator.x.discovery.ServiceDiscovery;
import org.apache.curator.x.discovery.ServiceDiscoveryBuilder;
import org.apache.curator.x.discovery.ServiceType;
import org.apache.curator.x.discovery.UriSpec;
import org.apache.curator.x.discovery.details.InstanceSerializer;
import org.springframework.cloud.zookeeper.discovery.ZookeeperInstance;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * zookeeper实现
 *
 * @author zhouss
 * @since 2022-09-26
 */
public class ZkDiscoveryClient implements ServiceDiscoveryClient {
    private static final Logger LOGGER = LoggerFactory.getLogger();

    private final AtomicBoolean isStarted = new AtomicBoolean();

    private final LbConfig lbConfig;

    /**
     * 当zk状态存在问题时, 使用异步尝试重试, 此处为重试时间间隔
     */
    private final long registryRetryInterval;

    /**
     * 当zk状态存在问题时, 使用异步尝试重试, 此处为最大从事次数
     */
    private final int registryMaxRetry;

    private ZkClient zkClient;

    private ServiceDiscovery<ZookeeperInstance> serviceDiscovery;

    private org.apache.curator.x.discovery.ServiceInstance<ZookeeperInstance> instance;

    /**
     * zk客户端
     */
    public ZkDiscoveryClient() {
        this.lbConfig = PluginConfigManager.getPluginConfig(LbConfig.class);
        this.registryRetryInterval = this.lbConfig.getRegistryRetryInterval();
        this.registryMaxRetry = this.lbConfig.getRegistryMaxRetry();
    }

    @Override
    public void init() {
    }

    @Override
    public boolean registry(ServiceInstance serviceInstance) {
        checkDiscoveryState();
        if (getZkClient().isStateOk()) {
            return registrySync(serviceInstance);
        }
        return registryAsync(serviceInstance);
    }

    private boolean registrySync(ServiceInstance serviceInstance) {
        final String id = UUID.randomUUID().toString();
        final HashMap<String, String> metadata = new HashMap<>(serviceInstance.getMetadata());
        metadata.put(LbConstants.SERMANT_DISCOVERY, "zk-" + id);
        final ZookeeperInstance zookeeperServiceInstance =
                new ZookeeperInstance(getAddress(serviceInstance) + ":" + serviceInstance.getPort(),
                        serviceInstance.getServiceName(), metadata);
        instance = new org.apache.curator.x.discovery.ServiceInstance<>(
                serviceInstance.getServiceName(), id,
                getAddress(serviceInstance),
                serviceInstance.getPort(),
                null, zookeeperServiceInstance, System.currentTimeMillis(), ServiceType.DYNAMIC,
                new UriSpec(lbConfig.getZkUriSpec()));
        try {
            this.serviceDiscovery.registerService(instance);
            return true;
        } catch (Exception exception) {
            LOGGER.log(Level.SEVERE, "Can not register service to zookeeper", exception);
        }
        return false;
    }

    private boolean registryAsync(ServiceInstance serviceInstance) {
        final AtomicBoolean isRegistrySuccess = new AtomicBoolean();
        CompletableFuture.runAsync(() -> {
            int tryNum = 0;
            while (tryNum++ <= registryMaxRetry) {
                if (getZkClient().isStateOk()) {
                    isRegistrySuccess.set(registrySync(serviceInstance));
                    LOGGER.info("Registry instance to zookeeper registry center success!");
                    break;
                }
                try {
                    Thread.sleep(registryRetryInterval);
                } catch (InterruptedException e) {
                    // ignored, 不可能会被打断
                }
            }
        }).whenComplete((unused, throwable) -> {
            if (!isRegistrySuccess.get()) {
                LOGGER.info("Registry instance to zookeeper registry center failed!");
            }
        });
        return false;
    }

    private String getAddress(ServiceInstance serviceInstance) {
        return lbConfig.isPreferIpAddress() ? serviceInstance.getIp() : serviceInstance.getHost();
    }

    @Override
    public Collection<ServiceInstance> getInstances(String serviceId) throws QueryInstanceException {
        if (!getZkClient().isStateOk()) {
            throw new QueryInstanceException("zk state is not valid!");
        }
        checkDiscoveryState();
        final ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(ZkDiscoveryClient.class.getClassLoader());
            return convert(serviceDiscovery.queryForInstances(serviceId));
        } catch (Exception exception) {
            LOGGER.log(Level.WARNING, "Can not query service instances from registry center!", exception);
            Thread.currentThread().setContextClassLoader(contextClassLoader);
            throw new QueryInstanceException(exception.getMessage());
        } finally {
            Thread.currentThread().setContextClassLoader(contextClassLoader);
        }
    }

    private Collection<ServiceInstance> convert(
            Collection<org.apache.curator.x.discovery.ServiceInstance<ZookeeperInstance>> serviceInstances) {
        if (serviceInstances == null || serviceInstances.isEmpty()) {
            return Collections.emptyList();
        }

        return serviceInstances.stream()
                .filter(serviceInstance -> ZkInstanceHelper.predicate(lbConfig.isOnlyCurRegisterInstances())
                        .test(serviceInstance))
                .map(ZkInstanceHelper::convert2Instance).distinct()
                .collect(Collectors.toList());
    }

    @Override
    public Collection<String> getServices() {
        check("get services from zookeeper");
        try {
            return serviceDiscovery.queryForNames();
        } catch (Exception exception) {
            LOGGER.log(Level.WARNING, "Can not query services from registry center!", exception);
        }
        return Collections.emptyList();
    }

    @Override
    public boolean unRegistry() {
        check("un registry from zookeeper");
        if (instance != null) {
            try {
                this.serviceDiscovery.unregisterService(instance);
                return true;
            } catch (Exception exception) {
                LOGGER.log(Level.WARNING, "Can not un registry from zookeeper center!", exception);
            }
        }
        return false;
    }

    @Override
    public String name() {
        return "Zookeeper";
    }

    private ServiceDiscovery<ZookeeperInstance> build() {
        return ServiceDiscoveryBuilder.builder(ZookeeperInstance.class)
                .client(getZkClient().getClient())
                .basePath(lbConfig.getZkBasePath())
                .serializer(new ZkInstanceSerializer<>(ZookeeperInstance.class))
                .watchInstances(false)
                .build();
    }

    private void check(String msg) {
        checkClientState(msg);
        checkDiscoveryState();
    }

    private void checkClientState(String msg) {
        if (!getZkClient().isStateOk()) {
            throw new IllegalStateException("Zookeeper state is not valid when " + msg);
        }
    }

    private void checkDiscoveryState() {
        if (isStarted.compareAndSet(false, true)) {
            // 初始化
            this.serviceDiscovery = build();
            try {
                this.serviceDiscovery.start();
            } catch (Exception exception) {
                LOGGER.log(Level.SEVERE, "Can not start zookeeper discovery client!", exception);
            }
        }
    }

    private ZkClient getZkClient() {
        if (zkClient != null) {
            return zkClient;
        }
        zkClient = PluginServiceManager.getPluginService(ZkClient.class);
        return zkClient;
    }

    @Override
    public void close() {
        if (this.serviceDiscovery == null) {
            return;
        }
        try {
            this.serviceDiscovery.close();
        } catch (IOException ex) {
            LOGGER.log(Level.WARNING, "Stop zookeeper discovery client failed", ex);
        }
    }

    /**
     * 自定义zk序列化器, 而非使用原生的{@link org.apache.curator.x.discovery.details.JsonInstanceSerializer} 避免漏洞问题, 替换ObjectMapper
     *
     * @param <T> 实例类型
     * @since 2022-10-08
     */
    public static class ZkInstanceSerializer<T> implements InstanceSerializer<T> {
        private final ObjectMapper mapper;

        private final Class<T> payloadClass;

        private final JavaType type;

        /**
         * 构造器
         *
         * @param payloadClass 指定payload类型
         */
        public ZkInstanceSerializer(Class<T> payloadClass) {
            this.payloadClass = payloadClass;
            mapper = new ObjectMapper();
            type = mapper.getTypeFactory().constructType(WriteAbleServiceInstance.class);
        }

        @Override
        public org.apache.curator.x.discovery.ServiceInstance<T> deserialize(byte[] bytes) throws Exception {
            WriteAbleServiceInstance<T> rawServiceInstance = mapper.readValue(bytes, type);
            payloadClass.cast(rawServiceInstance.getPayload());
            return rawServiceInstance;
        }

        @Override
        public byte[] serialize(org.apache.curator.x.discovery.ServiceInstance<T> instance) throws Exception {
            return mapper.writeValueAsBytes(new WriteAbleServiceInstance<>(instance));
        }
    }

    /**
     * 序列化的实例, 标记payload类型, 便于与其他开源的zk数据可互相识别
     *
     * @param <T> 实例类型
     * @since 2022-10-08
     */
    public static class WriteAbleServiceInstance<T> extends org.apache.curator.x.discovery.ServiceInstance<T> {
        /**
         * 构造器
         *
         * @param instance 实例信息
         */
        public WriteAbleServiceInstance(org.apache.curator.x.discovery.ServiceInstance<T> instance) {
            super(instance.getName(), instance.getId(), instance.getAddress(), instance.getPort(),
                    instance.getSslPort(), instance.getPayload(),
                    instance.getRegistrationTimeUTC(),
                    instance.getServiceType(),
                    instance.getUriSpec());
        }

        WriteAbleServiceInstance() {
            super("", "", null, null, null, null, 0, ServiceType.DYNAMIC, null, true);
        }

        @Override
        @JsonTypeInfo(use = Id.CLASS, defaultImpl = Object.class)
        public T getPayload() {
            return super.getPayload();
        }
    }
}
