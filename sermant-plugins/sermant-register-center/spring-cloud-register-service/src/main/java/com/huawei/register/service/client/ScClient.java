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

package com.huawei.register.service.client;

import com.huawei.register.config.RegisterConfig;
import com.huawei.sermant.core.common.LoggerFactory;
import com.huawei.sermant.core.lubanops.integration.utils.APMThreadFactory;
import com.huawei.sermant.core.plugin.config.PluginConfigManager;

import org.apache.servicecomb.foundation.auth.SignRequest;
import org.apache.servicecomb.http.client.auth.RequestAuthHeaderProvider;
import org.apache.servicecomb.http.client.common.HttpConfiguration;
import org.apache.servicecomb.service.center.client.AddressManager;
import org.apache.servicecomb.service.center.client.ServiceCenterClient;
import org.apache.servicecomb.service.center.client.ServiceCenterOperation;
import org.apache.servicecomb.service.center.client.exception.OperationException;
import org.apache.servicecomb.service.center.client.model.Framework;
import org.apache.servicecomb.service.center.client.model.Microservice;
import org.apache.servicecomb.service.center.client.model.MicroserviceInstance;
import org.apache.servicecomb.service.center.client.model.MicroserviceInstanceStatus;
import org.apache.servicecomb.service.center.client.model.MicroserviceInstancesResponse;
import org.apache.servicecomb.service.center.client.model.MicroserviceStatus;
import org.apache.servicecomb.service.center.client.model.RegisteredMicroserviceInstanceResponse;
import org.apache.servicecomb.service.center.client.model.RegisteredMicroserviceResponse;
import org.springframework.cloud.client.serviceregistry.Registration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * sc客户端初始化服务
 *
 * @author zhouss
 * @since 2021-12-15
 */
public class ScClient {
    /**
     * 日志
     */
    private static final Logger LOGGER = LoggerFactory.getLogger();

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

    /**
     * 服务/实例ID
     * Map<服务ID, 实例IDS>
     */
    private final Map<String, List<String>> serviceInstanceIdCache = new HashMap<String, List<String>>();

    private ServiceCenterClient client;

    private RegisterConfig registerConfig;

    private HeartbeatTask heartbeatTask;

    /**
     * 初始化启动方法
     */
    public void start() {
        registerConfig = PluginConfigManager.getPluginConfig(RegisterConfig.class);
        initScClient();
        heartbeatTask = new HeartbeatTask();
        heartbeatTask.start();
    }

    /**
     * 停止方法
     */
    public void stop() {
        heartbeatTask.stop();
    }

    /**
     * 注册sc服务与实例
     *
     * @param registration 注册信息
     */
    public void register(Registration registration) {
        final Microservice microservice = buildMicroService(registration);
        RegisteredMicroserviceResponse response = client.queryServiceId(microservice);
        if (response == null) {
            response = client.registerMicroservice(microservice);
        }
        String serviceId = response.getServiceId();
        final MicroserviceInstance microserviceInstance = buildMicroServiceInstance(registration, serviceId);
        final RegisteredMicroserviceInstanceResponse registeredMicroserviceInstanceResponse = client.registerMicroserviceInstance(microserviceInstance);
        if (registeredMicroserviceInstanceResponse != null) {
            updateCache(serviceId, registeredMicroserviceInstanceResponse.getInstanceId());
        }
    }

    private MicroserviceInstance buildMicroServiceInstance(Registration registration, String serviceId) {
        final MicroserviceInstance microserviceInstance = new MicroserviceInstance();
        microserviceInstance.setStatus(MicroserviceInstanceStatus.UP);
        microserviceInstance.setServiceId(serviceId);
        microserviceInstance.setVersion(registerConfig.getVersion());
        microserviceInstance.setHostName(registration.getHost());
        microserviceInstance.setEndpoints(buildEndpoints(registration));
        Map<String, String> meta = new HashMap<String, String>(registration.getMetadata());
        meta.put(REG_VERSION_KEY, registerConfig.getVersion());
        microserviceInstance.setProperties(meta);
        return microserviceInstance;
    }

    private List<String> buildEndpoints(Registration registration) {
        return Collections.singletonList(String.format(Locale.ENGLISH, "rest://%s:%d", registration.getHost(),
                registration.getPort()));
    }

    private Microservice buildMicroService(Registration registration) {
        final Microservice microservice = new Microservice();
        microservice.setAlias(registration.getServiceId());
        microservice.setAppId(registerConfig.getApplication());
        microservice.setEnvironment(registerConfig.getEnvironment());
        // agent相关信息
        final Framework framework = new Framework();
        framework.setName(registerConfig.getFramework());
        framework.setVersion(registerConfig.getFrameworkVersion());
        microservice.setFramework(framework);
        microservice.setVersion(registerConfig.getVersion());
        microservice.setServiceName(registration.getServiceId());
        microservice.setStatus(MicroserviceStatus.UP);
        return microservice;
    }

    /**
     * 查询所有实例
     *
     * @return 实例列表
     */
    public List<MicroserviceInstance> queryInstancesByServiceId(String serviceId) {
        MicroserviceInstancesResponse response = null;
        try {
            response = client.getMicroserviceInstanceList(serviceId);
        } catch (OperationException ex) {
            LOGGER.warning(String.format(Locale.ENGLISH,
                    "Query service center instance list failed! %s", ex.getMessage()));
        }
        return response == null ? Collections.<MicroserviceInstance>emptyList() :
                response.getInstances();
    }

    private void updateCache(String serviceId, String instanceId) {
        List<String> instanceIds = serviceInstanceIdCache.get(serviceId);
        if (instanceIds == null) {
            instanceIds = new ArrayList<String>();
        }
        instanceIds.add(instanceId);
        serviceInstanceIdCache.put(serviceId, instanceIds);
    }

    private void initScClient() {
        client = new ServiceCenterClient(
                createAddressManager(registerConfig.getProject(), getScUrls()),
                createSslProperties(registerConfig.isSslEnabled()),
                new RequestAuthHeaderProvider() {
                    @Override
                    public Map<String, String> loadAuthHeader(SignRequest signRequest) {
                        return Collections.emptyMap();
                    }
                },
                "default",
                Collections.<String, String>emptyMap());
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

    private HttpConfiguration.SSLProperties createSslProperties(boolean enabled) {
        final HttpConfiguration.SSLProperties sslProperties = new HttpConfiguration.SSLProperties();
        sslProperties.setEnabled(enabled);
        return sslProperties;
    }

    private AddressManager createAddressManager(String project, List<String> kieUrls) {
        return new AddressManager(project, kieUrls);
    }

    public ServiceCenterOperation getRawClient() {
        return client;
    }

    /**
     * 心跳发送任务
     */
    class HeartbeatTask {
        private final ScheduledThreadPoolExecutor scheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(1,
                new APMThreadFactory("REGISTER-HEARTBEAT-SEND-THREAD"));

        void start() {
            scheduledThreadPoolExecutor.scheduleAtFixedRate(new Runnable() {
                @Override
                public void run() {
                    if (serviceInstanceIdCache.isEmpty()) {
                        return;
                    }
                    final Set<Map.Entry<String, List<String>>> entries = serviceInstanceIdCache.entrySet();
                    for (Map.Entry<String, List<String>> entry : entries) {
                        final String serviceId = entry.getKey();
                        final List<String> instanceIds = entry.getValue();
                        if (instanceIds == null || instanceIds.isEmpty()) {
                            continue;
                        }
                        for (String instanceId : instanceIds) {
                            try {
                                client.sendHeartBeat(serviceId, instanceId);
                            } catch (Exception ex) {
                                LOGGER.warning(String.format(Locale.ENGLISH,
                                        "Sent heartbeat to sc failed! reason : %s", ex.getMessage()));
                            }
                        }
                    }
                }
            }, registerConfig.getHeartbeatInterval(), registerConfig.getHeartbeatInterval(), TimeUnit.SECONDS);
        }

        void stop() {
            scheduledThreadPoolExecutor.shutdown();
        }
    }
}
