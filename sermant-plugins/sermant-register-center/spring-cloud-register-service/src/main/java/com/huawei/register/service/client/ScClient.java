/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.huawei.register.service.client;

import com.huawei.register.config.RegisterConfig;
import com.huawei.register.context.RegisterContext;
import com.huawei.sermant.core.common.LoggerFactory;
import com.huawei.sermant.core.plugin.common.PluginConstant;
import com.huawei.sermant.core.plugin.common.PluginSchemaValidator;
import com.huawei.sermant.core.plugin.config.PluginConfigManager;
import com.huawei.sermant.core.utils.JarFileUtils;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import org.apache.servicecomb.http.client.common.HttpConfiguration;
import org.apache.servicecomb.service.center.client.AddressManager;
import org.apache.servicecomb.service.center.client.RegistrationEvents.HeartBeatEvent;
import org.apache.servicecomb.service.center.client.RegistrationEvents.MicroserviceRegistrationEvent;
import org.apache.servicecomb.service.center.client.ServiceCenterClient;
import org.apache.servicecomb.service.center.client.ServiceCenterDiscovery;
import org.apache.servicecomb.service.center.client.ServiceCenterOperation;
import org.apache.servicecomb.service.center.client.ServiceCenterRegistration;
import org.apache.servicecomb.service.center.client.exception.OperationException;
import org.apache.servicecomb.service.center.client.model.Framework;
import org.apache.servicecomb.service.center.client.model.HealthCheck;
import org.apache.servicecomb.service.center.client.model.HealthCheckMode;
import org.apache.servicecomb.service.center.client.model.Microservice;
import org.apache.servicecomb.service.center.client.model.MicroserviceInstance;
import org.apache.servicecomb.service.center.client.model.MicroserviceInstanceStatus;
import org.apache.servicecomb.service.center.client.model.MicroserviceInstancesResponse;
import org.apache.servicecomb.service.center.client.model.MicroserviceStatus;
import org.apache.servicecomb.service.center.client.model.ServiceCenterConfiguration;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.jar.JarFile;
import java.util.logging.Logger;

/**
 * 基于注册任务注册服务实例
 *
 * @author zhouss
 * @since 2022-02-25
 */
public class ScClient {
    /**
     * 日志
     */
    private static final Logger LOGGER = LoggerFactory.getLogger();

    /**
     * 事件栈
     */
    private static final EventBus EVENT_BUS = new EventBus();

    /**
     * http url前缀
     */
    private static final String HTTP_URL_PREFIX = "http://";

    /**
     * https url前缀
     */
    private static final String HTTPS_URL_PREFIX = "https://";

    /**
     * 注册版本号
     */
    private static final String REG_VERSION_KEY = "reg.version";

    private ServiceCenterConfiguration serviceCenterConfiguration;

    private ServiceCenterClient serviceCenterClient;

    private RegisterConfig registerConfig;

    private Microservice microservice;

    private ServiceCenterDiscovery serviceCenterDiscovery;

    /**
     * 初始化
     */
    public void init() {
        registerConfig = PluginConfigManager.getPluginConfig(RegisterConfig.class);
        initScClient();
        initServiceCenterConfiguration();
    }

    /**
     * 服务实例注册
     */
    public void register() {
        startServiceCenterRegistration();
    }

    /**
     * 查询所有实例
     *
     * @param serviceId 服务ID
     * @return 实例列表
     */
    public List<MicroserviceInstance> queryInstancesByServiceId(String serviceId) {
        MicroserviceInstancesResponse response = null;
        try {
            response = serviceCenterClient.getMicroserviceInstanceList(serviceId);
        } catch (OperationException ex) {
            LOGGER.warning(String.format(Locale.ENGLISH,
                "Query service center instance list failed! %s", ex.getMessage()));
        }
        return response == null ? Collections.emptyList() : response.getInstances();
    }

    /**
     * 获取内部client
     *
     * @return ServiceCenterOperation
     */
    public ServiceCenterOperation getRawClient() {
        return serviceCenterClient;
    }

    /**
     * 心跳事件
     *
     * @param event 心跳事件
     */
    @Subscribe
    public void onHeartBeatEvent(HeartBeatEvent event) {
        if (event.isSuccess()) {
            LOGGER.fine("Service center post heartbeat success!");
        }
    }

    /**
     * 注册事件
     *
     * @param event 注册事件
     */
    @Subscribe
    public void onMicroserviceRegistrationEvent(MicroserviceRegistrationEvent event) {
        if (event.isSuccess()) {
            if (serviceCenterDiscovery == null) {
                serviceCenterDiscovery = new ServiceCenterDiscovery(serviceCenterClient, EVENT_BUS);
                serviceCenterDiscovery.updateMyselfServiceId(microservice.getServiceId());
                serviceCenterDiscovery.setPollInterval(registerConfig.getPullInterval());
                serviceCenterDiscovery.startDiscovery();
            } else {
                serviceCenterDiscovery.updateMyselfServiceId(microservice.getServiceId());
            }
        }
    }

    private void startServiceCenterRegistration() {
        if (serviceCenterClient == null) {
            return;
        }
        ServiceCenterRegistration serviceCenterRegistration = new ServiceCenterRegistration(serviceCenterClient,
            serviceCenterConfiguration, EVENT_BUS);
        EVENT_BUS.register(this);
        serviceCenterRegistration.setMicroservice(buildMicroService());
        final MicroserviceInstance microserviceInstance = buildMicroServiceInstance();
        serviceCenterRegistration.setHeartBeatInterval(microserviceInstance.getHealthCheck().getInterval());
        serviceCenterRegistration.setMicroserviceInstance(microserviceInstance);
        serviceCenterRegistration.startRegistration();
    }

    private void initServiceCenterConfiguration() {
        serviceCenterConfiguration = new ServiceCenterConfiguration();
        serviceCenterConfiguration.setIgnoreSwaggerDifferent(false);
    }

    private void initScClient() {
        serviceCenterClient = new ServiceCenterClient(
            createAddressManager(registerConfig.getProject(), getScUrls()),
            createSslProperties(registerConfig.isSslEnabled()),
            signRequest -> Collections.emptyMap(),
            "default",
            Collections.emptyMap());
    }

    private List<String> buildEndpoints() {
        return Collections.singletonList(String.format(Locale.ENGLISH, "rest://%s:%d",
            RegisterContext.INSTANCE.getClientInfo().getHost(),
            RegisterContext.INSTANCE.getClientInfo().getPort()));
    }

    private MicroserviceInstance buildMicroServiceInstance() {
        final MicroserviceInstance microserviceInstance = new MicroserviceInstance();
        microserviceInstance.setStatus(MicroserviceInstanceStatus.UP);
        microserviceInstance.setHostName(RegisterContext.INSTANCE.getClientInfo().getHost());
        microserviceInstance.setEndpoints(buildEndpoints());
        HealthCheck healthCheck = new HealthCheck();
        healthCheck.setMode(HealthCheckMode.pull);
        healthCheck.setInterval(registerConfig.getHeartbeatInterval());
        healthCheck.setTimes(registerConfig.getHeartbeatRetryTimes());
        microserviceInstance.setHealthCheck(healthCheck);
        Map<String, String> meta = new HashMap<>(RegisterContext.INSTANCE.getClientInfo().getMeta());
        meta.put(REG_VERSION_KEY, registerConfig.getVersion());
        microserviceInstance.setProperties(meta);
        return microserviceInstance;
    }

    private String getVersion() {
        try (JarFile jarFile = new JarFile(getClass().getProtectionDomain().getCodeSource().getLocation().getPath())) {
            final Object pluginName = JarFileUtils.getManifestAttr(jarFile, PluginConstant.PLUGIN_NAME_KEY);
            if (pluginName instanceof String) {
                return PluginSchemaValidator.getPluginVersionMap().get(pluginName);
            }
        } catch (IOException e) {
            LOGGER.warning("Cannot not get the version.");
        }
        return "";
    }

    private Microservice buildMicroService() {
        microservice = new Microservice();
        microservice.setAlias(RegisterContext.INSTANCE.getClientInfo().getServiceId());
        microservice.setAppId(registerConfig.getApplication());
        microservice.setEnvironment(registerConfig.getEnvironment());

        // agent相关信息
        final Framework framework = new Framework();
        framework.setName(registerConfig.getFramework());
        framework.setVersion(getVersion());
        microservice.setFramework(framework);
        microservice.setVersion(registerConfig.getVersion());
        microservice.setServiceName(RegisterContext.INSTANCE.getClientInfo().getServiceId());
        microservice.setStatus(MicroserviceStatus.UP);
        return microservice;
    }

    private List<String> getScUrls() {
        final List<String> urlList = registerConfig.getAddressList();
        if (urlList == null || urlList.isEmpty()) {
            throw new IllegalArgumentException("Kie url must not be empty!");
        }
        Iterator<String> it = urlList.iterator();
        while (it.hasNext()) {
            String url = it.next();
            if (!isUrlValid(url)) {
                LOGGER.warning(String.format(Locale.ENGLISH, "Invalid url : %s", url));
                it.remove();
            }
        }
        return urlList;
    }

    private boolean isUrlValid(String url) {
        if (url == null || url.length() == 0) {
            return false;
        }
        final String trimUrl = url.trim();
        return trimUrl.startsWith(HTTP_URL_PREFIX) || trimUrl.startsWith(HTTPS_URL_PREFIX);
    }

    private HttpConfiguration.SSLProperties createSslProperties(boolean isEnabled) {
        final HttpConfiguration.SSLProperties sslProperties = new HttpConfiguration.SSLProperties();
        sslProperties.setEnabled(isEnabled);
        return sslProperties;
    }

    private AddressManager createAddressManager(String project, List<String> kieUrls) {
        return new AddressManager(project, kieUrls);
    }
}
