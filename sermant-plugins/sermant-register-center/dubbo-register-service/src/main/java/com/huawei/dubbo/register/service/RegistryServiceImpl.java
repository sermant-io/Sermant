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

package com.huawei.dubbo.register.service;

import com.huawei.dubbo.register.Subscription;
import com.huawei.dubbo.register.SubscriptionData;
import com.huawei.dubbo.register.SubscriptionKey;
import com.huawei.dubbo.register.cache.DubboCache;
import com.huawei.register.config.RegisterConfig;
import com.huawei.sermant.core.lubanops.bootstrap.log.LogFactory;
import com.huawei.sermant.core.plugin.PluginManager;
import com.huawei.sermant.core.plugin.common.PluginConstant;
import com.huawei.sermant.core.plugin.config.PluginConfigManager;
import com.huawei.sermant.core.util.JarFileUtil;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.dubbo.common.URL;
import org.apache.dubbo.registry.NotifyListener;
import org.apache.servicecomb.http.client.auth.DefaultRequestAuthHeaderProvider;
import org.apache.servicecomb.http.client.common.HttpConfiguration.SSLProperties;
import org.apache.servicecomb.service.center.client.AddressManager;
import org.apache.servicecomb.service.center.client.DiscoveryEvents.InstanceChangedEvent;
import org.apache.servicecomb.service.center.client.RegistrationEvents.HeartBeatEvent;
import org.apache.servicecomb.service.center.client.RegistrationEvents.MicroserviceInstanceRegistrationEvent;
import org.apache.servicecomb.service.center.client.RegistrationEvents.MicroserviceRegistrationEvent;
import org.apache.servicecomb.service.center.client.ServiceCenterClient;
import org.apache.servicecomb.service.center.client.ServiceCenterDiscovery;
import org.apache.servicecomb.service.center.client.ServiceCenterRegistration;
import org.apache.servicecomb.service.center.client.model.Framework;
import org.apache.servicecomb.service.center.client.model.HealthCheck;
import org.apache.servicecomb.service.center.client.model.HealthCheckMode;
import org.apache.servicecomb.service.center.client.model.Microservice;
import org.apache.servicecomb.service.center.client.model.MicroserviceInstance;
import org.apache.servicecomb.service.center.client.model.MicroserviceInstanceStatus;
import org.apache.servicecomb.service.center.client.model.MicroserviceInstancesResponse;
import org.apache.servicecomb.service.center.client.model.MicroservicesResponse;
import org.apache.servicecomb.service.center.client.model.SchemaInfo;
import org.apache.servicecomb.service.center.client.model.ServiceCenterConfiguration;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * 注册服务类
 *
 * @author provenceee
 * @date 2021/12/15
 */
public class RegistryServiceImpl implements RegistryService {
    private static final Logger LOGGER = LogFactory.getLogger();
    private static final EventBus EVENT_BUS = new EventBus();
    private static final Map<String, Microservice> INTERFACE_MAP = new ConcurrentHashMap<>();
    private static final Map<SubscriptionKey, SubscriptionData> SUBSCRIPTIONS = new ConcurrentHashMap<>();
    private static final CountDownLatch FIRST_REGISTRATION_WAITER = new CountDownLatch(1);
    private static final List<Subscription> PENDING_SUBSCRIBE_EVENT = new CopyOnWriteArrayList<>();
    private static final AtomicBoolean SHUTDOWN = new AtomicBoolean();
    private static final String FRAMEWORK_NAME = "sermant";
    private static final String DEFAULT_TENANT_NAME = "default";
    private static final String CONSUMER_PROTOCOL_PREFIX = "consumer";
    private final List<URL> registryUrls = new ArrayList<>();
    private ServiceCenterClient client;
    private Microservice microservice;
    private MicroserviceInstance microserviceInstance;
    private ServiceCenterRegistration serviceCenterRegistration;
    private ServiceCenterDiscovery serviceCenterDiscovery;
    private boolean registrationInProgress = true;
    private RegisterConfig config;

    @Override
    public void startRegistration() {
        if (!DubboCache.INSTANCE.isLoadSc()) {
            // 没有加载sc的注册spi就直接return
            return;
        }
        config = PluginConfigManager.getPluginConfig(RegisterConfig.class);
        client = new ServiceCenterClient(new AddressManager(config.getProject(), config.getAddressList()),
                new SSLProperties(), new DefaultRequestAuthHeaderProvider(), DEFAULT_TENANT_NAME,
                Collections.emptyMap());
        createMicroservice();
        createMicroserviceInstance();
        createServiceCenterRegistration();
        EVENT_BUS.register(this);
        serviceCenterRegistration.startRegistration();
        waitRegistrationDone();
    }

    @Override
    public void doSubscribe(URL url, NotifyListener notifyListener) {
        if (!CONSUMER_PROTOCOL_PREFIX.equals(url.getProtocol())) {
            return;
        }
        Subscription subscription = new Subscription(url, notifyListener);
        if (registrationInProgress) {
            PENDING_SUBSCRIBE_EVENT.add(subscription);
            return;
        }
        subscribe(subscription);
    }

    @Override
    public void shutdown() {
        if (!SHUTDOWN.compareAndSet(false, true)) {
            return;
        }
        if (serviceCenterRegistration != null) {
            serviceCenterRegistration.stop();
        }
        if (serviceCenterDiscovery != null) {
            serviceCenterDiscovery.stop();
        }
        if (client != null) {
            client.deleteMicroserviceInstance(microservice.getServiceId(), microserviceInstance.getInstanceId());
        }
    }

    @Override
    public void addRegistryUrls(URL url) {
        if (!CONSUMER_PROTOCOL_PREFIX.equals(url.getProtocol())) {
            registryUrls.add(url);
        }
    }

    private void createMicroservice() {
        microservice = new Microservice(DubboCache.INSTANCE.getServiceName());
        microservice.setAppId(config.getApplication());
        microservice.setVersion(config.getVersion());
        microservice.setEnvironment(config.getEnvironment());
        Framework framework = new Framework();
        framework.setName(FRAMEWORK_NAME);
        framework.setVersion(getVersion());
        microservice.setFramework(framework);
        microservice.setSchemas(registryUrls.stream().map(URL::getPath).distinct().collect(Collectors.toList()));
    }

    private String getVersion() {
        try (JarFile jarFile = new JarFile(getClass().getProtectionDomain().getCodeSource().getLocation().getPath())) {
            String pluginName = (String) JarFileUtil.getManifestAttr(jarFile, PluginConstant.PLUGIN_NAME_KEY);
            return PluginManager.getPluginVersionMap().get(pluginName);
        } catch (IOException e) {
            LOGGER.warning("Cannot not get the version.");
            return "";
        }
    }

    private void createMicroserviceInstance() {
        microserviceInstance = new MicroserviceInstance();
        microserviceInstance.setStatus(MicroserviceInstanceStatus.UP);
        HealthCheck healthCheck = new HealthCheck();
        healthCheck.setMode(HealthCheckMode.pull);
        healthCheck.setInterval(config.getHeartbeatInterval());
        healthCheck.setTimes(config.getHeartbeatRetryTimes());
        microserviceInstance.setHealthCheck(healthCheck);
        microserviceInstance.setHostName(getHost());
        microserviceInstance.setEndpoints(getEndpoints());
    }

    private List<String> getEndpoints() {
        return registryUrls.stream()
                .map(url -> new URL(url.getProtocol(), url.getHost(), url.getPort()).toString()).distinct()
                .collect(Collectors.toList());
    }

    private void createServiceCenterRegistration() {
        ServiceCenterConfiguration serviceCenterConfiguration = new ServiceCenterConfiguration();
        serviceCenterConfiguration.setIgnoreSwaggerDifferent(false);
        serviceCenterRegistration = new ServiceCenterRegistration(client, serviceCenterConfiguration, EVENT_BUS);
        serviceCenterRegistration.setMicroservice(microservice);
        serviceCenterRegistration.setMicroserviceInstance(microserviceInstance);
        serviceCenterRegistration.setHeartBeatInterval(microserviceInstance.getHealthCheck().getInterval());
        serviceCenterRegistration
                .setSchemaInfos(registryUrls.stream().map(this::createSchemaInfo).collect(Collectors.toList()));
    }

    private String getHost() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            throw new RuntimeException("Cannot get the host.");
        }
    }

    private SchemaInfo createSchemaInfo(URL url) {
        URL newUrl = url.setHost(microservice.getServiceName());
        return new SchemaInfo(newUrl.getPath(), newUrl.toString(), DigestUtils.sha256Hex(newUrl.toString()));
    }

    private void subscribe(Subscription subscription) {
        Microservice service = INTERFACE_MAP.get(subscription.getUrl().getPath());
        if (service == null) {
            updateInterfaceMap();
            service = INTERFACE_MAP.get(subscription.getUrl().getPath());
        }
        if (service == null) {
            LOGGER.log(Level.WARNING, "the subscribe url [{}] is not registered.", subscription.getUrl().getPath());
            PENDING_SUBSCRIBE_EVENT.add(subscription);
            return;
        }
        MicroserviceInstancesResponse response = client.getMicroserviceInstanceList(service.getServiceId());
        SUBSCRIPTIONS.put(new SubscriptionKey(service.getAppId(), service.getServiceName(),
                        subscription.getUrl().getPath()),
                new SubscriptionData(subscription.getNotifyListener(), new ArrayList<>()));
        notify(service.getAppId(), service.getServiceName(), response.getInstances());
        serviceCenterDiscovery.registerIfNotPresent(
                new ServiceCenterDiscovery.SubscriptionKey(service.getAppId(), service.getServiceName()));
    }

    private void waitRegistrationDone() {
        try {
            FIRST_REGISTRATION_WAITER.await(30, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            LOGGER.log(Level.WARNING, "registration is not finished in 30 seconds.");
        }
    }

    private void updateInterfaceMap() {
        INTERFACE_MAP.clear();
        MicroservicesResponse microservicesResponse = client.getMicroserviceList();
        microservicesResponse.getServices().forEach(this::updateInterfaceMap);
    }

    private void updateInterfaceMap(Microservice service) {
        if (microservice.getAppId().equals(service.getAppId())) {
            service.getSchemas().forEach(schema -> INTERFACE_MAP.put(schema, service));
        }
    }

    private void processPendingEvent() {
        List<Subscription> events = new ArrayList<>(PENDING_SUBSCRIBE_EVENT);
        PENDING_SUBSCRIBE_EVENT.clear();
        events.forEach(this::subscribe);
    }

    /**
     * 心跳事件
     *
     * @param event 心跳事件
     */
    @Subscribe
    public void onHeartBeatEvent(HeartBeatEvent event) {
        if (event.isSuccess()) {
            registrationInProgress = false;
            processPendingEvent();
        }
    }

    /**
     * 注册事件
     *
     * @param event 注册事件
     */
    @Subscribe
    public void onMicroserviceRegistrationEvent(MicroserviceRegistrationEvent event) {
        registrationInProgress = true;
        if (event.isSuccess()) {
            if (serviceCenterDiscovery == null) {
                serviceCenterDiscovery = new ServiceCenterDiscovery(client, EVENT_BUS);
                serviceCenterDiscovery.updateMyselfServiceId(microservice.getServiceId());
                serviceCenterDiscovery.setPollInterval(config.getPullInterval());
                serviceCenterDiscovery.startDiscovery();
            } else {
                serviceCenterDiscovery.updateMyselfServiceId(microservice.getServiceId());
            }
        }
    }

    /**
     * 注册事件
     *
     * @param event 注册事件
     */
    @Subscribe
    public void onMicroserviceInstanceRegistrationEvent(MicroserviceInstanceRegistrationEvent event) {
        registrationInProgress = true;
        if (event.isSuccess()) {
            updateInterfaceMap();
            FIRST_REGISTRATION_WAITER.countDown();
        }
    }

    /**
     * 实例变化事件
     *
     * @param event 实例变化事件
     */
    @Subscribe
    public void onInstanceChangedEvent(InstanceChangedEvent event) {
        notify(event.getAppName(), event.getServiceName(), event.getInstances());
    }

    private void notify(String appId, String serviceName, List<MicroserviceInstance> instances) {
        if (instances != null) {
            Map<String, List<URL>> notifyUrls = instancesToUrls(instances);
            notifyUrls.forEach((path, urls) -> {
                SubscriptionKey subscriptionKey = new SubscriptionKey(appId, serviceName, path);
                SubscriptionData subscriptionData = SUBSCRIPTIONS.get(subscriptionKey);
                if (subscriptionData != null) {
                    subscriptionData.getUrls().clear();
                    subscriptionData.getUrls().addAll(urls);
                    subscriptionData.getNotifyListener().notify(urls);
                }
            });
        }
    }

    private Map<String, List<URL>> instancesToUrls(List<MicroserviceInstance> instances) {
        Map<String, List<URL>> urlMap = new HashMap<>();
        instances.forEach(instance -> convertToUrlMap(urlMap, instance));
        return urlMap;
    }

    private void convertToUrlMap(Map<String, List<URL>> urlMap, MicroserviceInstance instance) {
        List<SchemaInfo> schemaInfos = client.getServiceSchemasList(instance.getServiceId(), true);
        instance.getEndpoints().forEach(endpoint -> {
            URL url = URL.valueOf(endpoint);
            if (schemaInfos.isEmpty()) {
                urlMap.computeIfAbsent(url.getPath(), value -> new ArrayList<>()).add(url);
                return;
            }
            schemaInfos.forEach(schema -> {
                URL newUrl = URL.valueOf(schema.getSchema());
                if (!newUrl.getProtocol().equals(url.getProtocol())) {
                    return;
                }
                urlMap.computeIfAbsent(newUrl.getPath(), value -> new ArrayList<>())
                        .add(newUrl.setAddress(url.getAddress()));
            });
        });
    }
}