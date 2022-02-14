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
import com.huawei.dubbo.register.SubscriptionKey;
import com.huawei.dubbo.register.cache.DubboCache;
import com.huawei.dubbo.register.constants.Constant;
import com.huawei.dubbo.register.utils.ReflectUtils;
import com.huawei.register.config.RegisterConfig;
import com.huawei.sermant.core.lubanops.bootstrap.log.LogFactory;
import com.huawei.sermant.core.lubanops.bootstrap.utils.StringUtils;
import com.huawei.sermant.core.plugin.common.PluginConstant;
import com.huawei.sermant.core.plugin.common.PluginSchemaValidator;
import com.huawei.sermant.core.plugin.config.PluginConfigManager;
import com.huawei.sermant.core.utils.JarFileUtils;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import org.apache.commons.codec.digest.DigestUtils;
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
import java.util.Objects;
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
 * 注册服务类，代码中使用反射调用类方法是为了同时兼容alibaba和apache dubbo
 *
 * @author provenceee
 * @since 2021/12/15
 */
@SuppressWarnings({"checkstyle:RegexpSingleline", "checkstyle:RegexpMultiline"})
public class RegistryServiceImpl implements RegistryService {
    private static final Logger LOGGER = LogFactory.getLogger();
    private static final EventBus EVENT_BUS = new EventBus();
    private static final Map<String, Microservice> INTERFACE_MAP = new ConcurrentHashMap<>();
    private static final Map<SubscriptionKey, Object> SUBSCRIPTIONS = new ConcurrentHashMap<>();
    private static final CountDownLatch FIRST_REGISTRATION_WAITER = new CountDownLatch(1);
    private static final int REGISTRATION_WAITE_TIME = 30;
    private static final List<Subscription> PENDING_SUBSCRIBE_EVENT = new CopyOnWriteArrayList<>();
    private static final AtomicBoolean SHUTDOWN = new AtomicBoolean();
    private static final String FRAMEWORK_NAME = "sermant";
    private static final String DEFAULT_TENANT_NAME = "default";
    private static final String CONSUMER_PROTOCOL_PREFIX = "consumer";
    private final List<Object> registryUrls = new ArrayList<>();
    private ServiceCenterClient client;
    private Microservice microservice;
    private MicroserviceInstance microserviceInstance;
    private ServiceCenterRegistration serviceCenterRegistration;
    private ServiceCenterDiscovery serviceCenterDiscovery;
    private boolean isRegistrationInProgress = true;
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

    /**
     * 订阅接口
     *
     * @param url 订阅地址
     * @param notifyListener 实例通知监听器
     * @see com.alibaba.dubbo.common.URL
     * @see org.apache.dubbo.common.URL
     * @see com.alibaba.dubbo.registry.NotifyListener
     * @see org.apache.dubbo.registry.NotifyListener
     */
    @Override
    public void doSubscribe(Object url, Object notifyListener) {
        if (!CONSUMER_PROTOCOL_PREFIX.equals(ReflectUtils.getProtocol(url))) {
            return;
        }
        Subscription subscription = new Subscription(url, notifyListener);
        if (isRegistrationInProgress) {
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

    /**
     * 增加注册接口
     *
     * @param url 注册url
     * @see com.alibaba.dubbo.common.URL
     * @see org.apache.dubbo.common.URL
     */
    @Override
    public void addRegistryUrls(Object url) {
        if (!CONSUMER_PROTOCOL_PREFIX.equals(ReflectUtils.getProtocol(url))) {
            registryUrls.add(url);
        }
    }

    /**
     * 心跳事件
     *
     * @param event 心跳事件
     */
    @Subscribe
    public void onHeartBeatEvent(HeartBeatEvent event) {
        if (event.isSuccess()) {
            isRegistrationInProgress = false;
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
        isRegistrationInProgress = true;
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
        isRegistrationInProgress = true;
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

    private void createMicroservice() {
        microservice = new Microservice(DubboCache.INSTANCE.getServiceName());
        microservice.setAppId(config.getApplication());
        microservice.setVersion(config.getVersion());
        microservice.setEnvironment(config.getEnvironment());
        Framework framework = new Framework();
        framework.setName(FRAMEWORK_NAME);
        framework.setVersion(getVersion());
        microservice.setFramework(framework);
        microservice.setSchemas(getSchemas());
    }

    private String getVersion() {
        try (JarFile jarFile = new JarFile(getClass().getProtectionDomain().getCodeSource().getLocation().getPath())) {
            String pluginName = (String) JarFileUtils.getManifestAttr(jarFile, PluginConstant.PLUGIN_NAME_KEY);
            return PluginSchemaValidator.getPluginVersionMap().get(pluginName);
        } catch (IOException e) {
            LOGGER.warning("Cannot not get the version.");
            return "";
        }
    }

    private List<String> getSchemas() {
        return registryUrls.stream().map(ReflectUtils::getPath).filter(StringUtils::isNotBlank).distinct()
            .collect(Collectors.toList());
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

    private String getHost() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            throw new RuntimeException("Cannot get the host.");
        }
    }

    private List<String> getEndpoints() {
        return registryUrls.stream().map(this::getUrl).filter(StringUtils::isNotBlank).distinct()
            .collect(Collectors.toList());
    }

    private String getUrl(Object url) {
        String protocol = ReflectUtils.getProtocol(url);
        if (StringUtils.isBlank(protocol)) {
            return null;
        }
        String address = ReflectUtils.getAddress(url);
        if (StringUtils.isBlank(address)) {
            return null;
        }
        Object endpoint = ReflectUtils.valueOf(protocol + Constant.PROTOCOL_SEPARATION + address);
        return endpoint == null ? null : endpoint.toString();
    }

    private void createServiceCenterRegistration() {
        ServiceCenterConfiguration serviceCenterConfiguration = new ServiceCenterConfiguration();
        serviceCenterConfiguration.setIgnoreSwaggerDifferent(false);
        serviceCenterRegistration = new ServiceCenterRegistration(client, serviceCenterConfiguration, EVENT_BUS);
        serviceCenterRegistration.setMicroservice(microservice);
        serviceCenterRegistration.setMicroserviceInstance(microserviceInstance);
        serviceCenterRegistration.setHeartBeatInterval(microserviceInstance.getHealthCheck().getInterval());
        serviceCenterRegistration.setSchemaInfos(getSchemaInfos());
    }

    private List<SchemaInfo> getSchemaInfos() {
        return registryUrls.stream().map(this::createSchemaInfo).filter(Objects::nonNull).collect(Collectors.toList());
    }

    private SchemaInfo createSchemaInfo(Object url) {
        Object newUrl = ReflectUtils.setHost(url, microservice.getServiceName());
        if (newUrl == null) {
            return null;
        }
        String schema = newUrl.toString();
        return new SchemaInfo(ReflectUtils.getPath(newUrl), schema, DigestUtils.sha256Hex(schema));
    }

    private void subscribe(Subscription subscription) {
        String path = ReflectUtils.getPath(subscription.getUrl());
        Microservice service = INTERFACE_MAP.get(path);
        if (service == null) {
            updateInterfaceMap();
            service = INTERFACE_MAP.get(path);
        }
        if (service == null) {
            LOGGER.log(Level.WARNING, "the subscribe url [{}] is not registered.", path);
            PENDING_SUBSCRIBE_EVENT.add(subscription);
            return;
        }
        String appId = service.getAppId();
        String serviceName = service.getServiceName();
        MicroserviceInstancesResponse response = client.getMicroserviceInstanceList(service.getServiceId());
        SUBSCRIPTIONS.put(new SubscriptionKey(appId, serviceName, path), subscription.getNotifyListener());
        notify(appId, serviceName, response.getInstances());
        serviceCenterDiscovery.registerIfNotPresent(new ServiceCenterDiscovery.SubscriptionKey(appId, serviceName));
    }

    private void waitRegistrationDone() {
        try {
            FIRST_REGISTRATION_WAITER.await(REGISTRATION_WAITE_TIME, TimeUnit.SECONDS);
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

    private void notify(String appId, String serviceName, List<MicroserviceInstance> instances) {
        if (instances != null) {
            Map<String, List<Object>> notifyUrls = instancesToUrls(instances);
            notifyUrls.forEach((path, urls) -> {
                SubscriptionKey subscriptionKey = new SubscriptionKey(appId, serviceName, path);
                Object notifyListener = SUBSCRIPTIONS.get(subscriptionKey);
                if (notifyListener != null) {
                    ReflectUtils.notify(notifyListener, urls);
                }
            });
        }
    }

    private Map<String, List<Object>> instancesToUrls(List<MicroserviceInstance> instances) {
        Map<String, List<Object>> urlMap = new HashMap<>();
        instances.forEach(instance -> convertToUrlMap(urlMap, instance));
        return urlMap;
    }

    private void convertToUrlMap(Map<String, List<Object>> urlMap, MicroserviceInstance instance) {
        List<SchemaInfo> schemaInfos = client.getServiceSchemasList(instance.getServiceId(), true);
        instance.getEndpoints().forEach(endpoint -> {
            Object url = ReflectUtils.valueOf(endpoint);
            if (schemaInfos.isEmpty()) {
                urlMap.computeIfAbsent(ReflectUtils.getPath(url), value -> new ArrayList<>()).add(url);
                return;
            }
            schemaInfos.forEach(schema -> {
                Object newUrl = ReflectUtils.valueOf(schema.getSchema());
                if (!Objects.equals(ReflectUtils.getProtocol(newUrl), ReflectUtils.getProtocol(url))) {
                    return;
                }
                urlMap.computeIfAbsent(ReflectUtils.getPath(newUrl), value -> new ArrayList<>())
                    .add(ReflectUtils.setAddress(newUrl, ReflectUtils.getAddress(url)));
            });
        });
    }
}