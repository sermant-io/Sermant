/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 * Based org/apache/servicecomb/service/center/client/ServiceCenterDiscovery.java
 * from the Apache ServiceComb Java Chassis project.
 */

package com.huawei.registry.service.client;

import com.huaweicloud.sermant.core.common.LoggerFactory;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import org.apache.servicecomb.http.client.task.AbstractTask;
import org.apache.servicecomb.http.client.task.Task;
import org.apache.servicecomb.service.center.client.DiscoveryEvents.InstanceChangedEvent;
import org.apache.servicecomb.service.center.client.DiscoveryEvents.PullInstanceEvent;
import org.apache.servicecomb.service.center.client.ServiceCenterClient;
import org.apache.servicecomb.service.center.client.ServiceCenterDiscovery;
import org.apache.servicecomb.service.center.client.exception.OperationException;
import org.apache.servicecomb.service.center.client.model.FindMicroserviceInstancesResponse;
import org.apache.servicecomb.service.center.client.model.Microservice;
import org.apache.servicecomb.service.center.client.model.MicroserviceInstance;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * service center实例发现, 原{@link ServiceCenterDiscovery}日志处理存在问题，因此此处重新实现
 *
 * @author zhouss
 * @since 2022-05-11
 */
public class ScDiscovery extends AbstractTask {
    private static final Logger LOGGER = LoggerFactory.getLogger();

    /**
     * 默认拉取间隔
     */
    private static final long DEFAULT_POLL_INTERVAL = 15000L;

    /**
     * endpoint最大长度，超过该长度直接截取
     */
    private static final int MAX_LEN_OF_ENDPOINT = 64;

    /**
     * 查询所有的版本号
     */
    private static final String ALL_VERSION = "0+";

    private final Object lock = new Object();

    private final ServiceCenterClient serviceCenterClient;

    private final EventBus eventBus;

    private final Map<SubscriptionKey, SubscriptionValue> instancesCache = new ConcurrentHashMap<>();

    private final Map<String, Microservice> microserviceCache = new ConcurrentHashMap<>();

    private final List<SubscriptionKey> failedInstances = new ArrayList<>();

    private String myselfServiceId;

    private long pollInterval;

    private boolean started = false;

    /**
     * 构造器
     *
     * @param serviceCenterClient sc客户端
     * @param eventBus            事件总栈
     */
    public ScDiscovery(ServiceCenterClient serviceCenterClient, EventBus eventBus) {
        super("service-center-discovery-task");
        this.serviceCenterClient = serviceCenterClient;
        this.eventBus = eventBus;
        this.eventBus.register(this);
        this.pollInterval = DEFAULT_POLL_INTERVAL;
    }

    /**
     * 设置拉取间隔
     *
     * @param interval 拉取间隔， 单位ms
     */
    public void setPollInterval(long interval) {
        if (interval > ServiceCenterDiscovery.MAX_INTERVAL || interval < ServiceCenterDiscovery.MIN_INTERVAL) {
            return;
        }
        this.pollInterval = interval;
    }

    /**
     * 更新自身服务ID
     *
     * @param curServiceId 自身服务编号
     */
    public void updateMyselfServiceId(String curServiceId) {
        this.myselfServiceId = curServiceId;
    }

    /**
     * 开启相关定时任务，拉取实例数据
     */
    public void startDiscovery() {
        if (!started) {
            started = true;
            startTask(new PullInstanceTask());
        }
    }

    /**
     * 注册订阅者
     *
     * @param subscriptionKey 订阅者
     */
    public void registerIfNotPresent(SubscriptionKey subscriptionKey) {
        if (this.instancesCache.get(subscriptionKey) == null) {
            synchronized (lock) {
                if (this.instancesCache.get(subscriptionKey) == null) {
                    SubscriptionValue value = new SubscriptionValue();
                    pullInstance(subscriptionKey, value, Level.INFO);
                    this.instancesCache.put(subscriptionKey, value);
                }
            }
        }
    }

    /**
     * 获取实例缓存
     *
     * @param key 订阅者
     * @return 实例数据
     */
    public List<MicroserviceInstance> getInstanceCache(SubscriptionKey key) {
        return this.instancesCache.get(key).instancesCache;
    }

    /**
     * 订阅拉取事件
     *
     * @param event 拉取事件
     */
    @Subscribe
    public void onPullInstanceEvent(PullInstanceEvent event) {
        pullAllInstance();
    }

    private void pullInstance(SubscriptionKey key, SubscriptionValue value, Level level) {
        if (myselfServiceId == null) {
            // registration not ready
            return;
        }
        try {
            FindMicroserviceInstancesResponse instancesResponse = serviceCenterClient
                .findMicroserviceInstance(myselfServiceId, key.appId, key.serviceName, ALL_VERSION, value.revision);
            if (instancesResponse.isModified()) {
                List<MicroserviceInstance> instances =
                    instancesResponse.getMicroserviceInstancesResponse().getInstances()
                        == null ? Collections.emptyList()
                        : instancesResponse.getMicroserviceInstancesResponse().getInstances();
                setMicroserviceInfo(instances);
                LOGGER.info(String.format(Locale.ENGLISH,
                    "Instance changed event, current: revision={%s}, instances={%s}; "
                        + "origin: revision={%s}, instances={%s}; appId={%s}, serviceName={%s}",
                    instancesResponse.getRevision(),
                    instanceToString(instances),
                    value.revision,
                    instanceToString(value.instancesCache),
                    key.appId,
                    key.serviceName
                ));
                value.instancesCache = instances;
                value.revision = instancesResponse.getRevision();
                eventBus.post(new InstanceChangedEvent(key.appId, key.serviceName,
                    value.instancesCache));
            }
        } catch (OperationException ex) {
            String message = String.format(Locale.ENGLISH,
                "find service {%s}#{%s} instance failed. caused by %s, if you are run with mode migration, please "
                    + "ignore it!", key.appId, key.serviceName,
                ex.getMessage());
            LOGGER.log(level, message);
            LOGGER.log(Level.FINE, message, ex);
            failedInstances.add(key);
        }
    }

    private void setMicroserviceInfo(List<MicroserviceInstance> instances) {
        instances.forEach(instance -> {
            Microservice microservice = microserviceCache
                .computeIfAbsent(instance.getServiceId(), id -> {
                    try {
                        return serviceCenterClient.getMicroserviceByServiceId(id);
                    } catch (OperationException ex) {
                        LOGGER.log(Level.INFO, String.format(Locale.ENGLISH,
                            "Find microservice by id={%s} failed", id), ex);
                        throw ex;
                    }
                });
            instance.setMicroservice(microservice);
        });
    }

    private synchronized void pullAllInstance() {
        instancesCache.forEach((key, value) -> this.pullInstance(key, value, Level.FINE));
        if (failedInstances.isEmpty()) {
            return;
        }
        failedInstances.forEach(instancesCache::remove);
        failedInstances.clear();
    }

    private static String instanceToString(List<MicroserviceInstance> instances) {
        if (instances == null) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        for (MicroserviceInstance instance : instances) {
            for (String endpoint : instance.getEndpoints()) {
                sb.append(endpoint.length() > MAX_LEN_OF_ENDPOINT
                    ? endpoint.substring(0, MAX_LEN_OF_ENDPOINT) : endpoint);
                sb.append("|");
            }
            sb.append(instance.getServiceName());
            sb.append("|");
        }
        sb.append("#");
        return sb.toString();
    }

    /**
     * 定时拉取任务
     *
     * @since 2022-05-11
     */
    class PullInstanceTask implements Task {
        @Override
        public void execute() {
            pullAllInstance();

            startTask(new BackOffSleepTask(pollInterval, new PullInstanceTask()));
        }
    }

    /**
     * 订阅实例
     *
     * @since 2022-05-11
     */
    public static class SubscriptionValue {
        String revision;

        List<MicroserviceInstance> instancesCache;
    }

    /**
     * 订阅者
     *
     * @since 2022-05-11
     */
    public static class SubscriptionKey {
        final String appId;

        final String serviceName;

        /**
         * 订阅者
         *
         * @param appId       appName
         * @param serviceName 服务名
         */
        public SubscriptionKey(String appId, String serviceName) {
            this.appId = appId;
            this.serviceName = serviceName;
        }

        @Override
        public boolean equals(Object target) {
            if (this == target) {
                return true;
            }
            if (target == null || getClass() != target.getClass()) {
                return false;
            }
            SubscriptionKey that = (SubscriptionKey) target;
            return appId.equals(that.appId) && serviceName.equals(that.serviceName);
        }

        @Override
        public int hashCode() {
            return Objects.hash(appId, serviceName);
        }
    }
}
