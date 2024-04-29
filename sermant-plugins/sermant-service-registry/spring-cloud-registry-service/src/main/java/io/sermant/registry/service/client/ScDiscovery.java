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

package io.sermant.registry.service.client;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import io.sermant.core.common.LoggerFactory;

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
 * The service center instance discovery;
 * that there was a problem with the original {@link ServiceCenterDiscovery} log
 * processing, so it is reimplemented here
 *
 * @author zhouss
 * @since 2022-05-11
 */
public class ScDiscovery extends AbstractTask {
    private static final Logger LOGGER = LoggerFactory.getLogger();

    /**
     * Default pull interval
     */
    private static final long DEFAULT_POLL_INTERVAL = 15000L;

    /**
     * The maximum length of the endpoint, which is directly intercepted after the endpoint
     */
    private static final int MAX_LEN_OF_ENDPOINT = 64;

    /**
     * Query all version numbers
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
     * Constructor
     *
     * @param serviceCenterClient SC client
     * @param eventBus Total stack of events
     */
    public ScDiscovery(ServiceCenterClient serviceCenterClient, EventBus eventBus) {
        super("service-center-discovery-task");
        this.serviceCenterClient = serviceCenterClient;
        this.eventBus = eventBus;
        this.eventBus.register(this);
        this.pollInterval = DEFAULT_POLL_INTERVAL;
    }

    /**
     * Set the pull interval
     *
     * @param interval Pull interval, in ms
     */
    public void setPollInterval(long interval) {
        if (interval > ServiceCenterDiscovery.MAX_INTERVAL || interval < ServiceCenterDiscovery.MIN_INTERVAL) {
            return;
        }
        this.pollInterval = interval;
    }

    /**
     * Update your service ID
     *
     * @param curServiceId Self Service Tag
     */
    public void updateMyselfServiceId(String curServiceId) {
        this.myselfServiceId = curServiceId;
    }

    /**
     * Enable a scheduled task to pull instance data
     */
    public void startDiscovery() {
        if (!started) {
            started = true;
            startTask(new PullInstanceTask());
        }
    }

    /**
     * Sign up for subscribers
     *
     * @param subscriptionKey Subscriber
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
     * Obtain the instance cache
     *
     * @param key Subscriber
     * @return Instance data
     */
    public List<MicroserviceInstance> getInstanceCache(SubscriptionKey key) {
        return this.instancesCache.get(key).instancesCache;
    }

    /**
     * Subscribe to pull events
     *
     * @param event Pull event
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
     * Pull tasks at regular intervals
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
     * Subscribe to an instance
     *
     * @since 2022-05-11
     */
    public static class SubscriptionValue {
        String revision;

        List<MicroserviceInstance> instancesCache;
    }

    /**
     * Subscriber
     *
     * @since 2022-05-11
     */
    public static class SubscriptionKey {
        final String appId;

        final String serviceName;

        /**
         * Subscriber
         *
         * @param appId appName
         * @param serviceName service name
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
