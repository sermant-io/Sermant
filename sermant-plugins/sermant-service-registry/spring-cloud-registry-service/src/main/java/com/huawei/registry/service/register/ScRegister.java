/*
 * Copyright (C) 2021-2021 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huawei.registry.service.register;

import com.huawei.registry.config.RegisterConfig;
import com.huawei.registry.config.RegisterType;
import com.huawei.registry.service.client.ScClient;

import com.huaweicloud.sermant.core.common.LoggerFactory;
import com.huaweicloud.sermant.core.plugin.config.PluginConfigManager;
import com.huaweicloud.sermant.core.utils.StringUtils;

import org.apache.servicecomb.service.center.client.exception.OperationException;
import org.apache.servicecomb.service.center.client.model.Microservice;
import org.apache.servicecomb.service.center.client.model.MicroserviceInstance;
import org.apache.servicecomb.service.center.client.model.MicroserviceInstanceStatus;
import org.apache.servicecomb.service.center.client.model.MicroservicesResponse;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * SC注册实现
 *
 * @author zhouss
 * @since 2021-12-17
 */
public class ScRegister implements Register {
    private ScClient client;

    @Override
    public void start() {
        client = new ScClient();
        client.init();
    }

    @Override
    public void stop() {
        client.deRegister();
    }

    @Override
    public void register() {
        client.register();
    }

    @Override
    public String getRegisterCenterStatus() {
        return client.getRegisterCenterStatus();
    }

    @Override
    public String getInstanceStatus() {
        return client.getInstanceStatus();
    }

    @Override
    public void updateInstanceStatus(String status) {
        client.updateInstanceStatus(status);
    }

    @Override
    public List<ServicecombServiceInstance> getInstanceList(String serviceId) {
        final List<MicroserviceInstance> microserviceInstances = getScInstances(serviceId);
        final List<ServicecombServiceInstance> serviceInstances = new ArrayList<>();
        for (final MicroserviceInstance microserviceInstance : microserviceInstances) {
            serviceInstances.add(new ServicecombServiceInstance(microserviceInstance));
        }
        return serviceInstances;
    }

    @Override
    public List<String> getServices() {
        List<String> serviceList = new ArrayList<>();
        try {
            MicroservicesResponse microServiceResponse = client.getRawClient().getMicroserviceList();
            if (microServiceResponse == null || microServiceResponse.getServices() == null) {
                return serviceList;
            }
            for (Microservice microservice : microServiceResponse.getServices()) {
                final Optional<String> microserviceName = getMicroserviceName(microservice);
                microserviceName.ifPresent(serviceList::add);
            }
        } catch (OperationException ex) {
            LoggerFactory.getLogger().severe(String.format(Locale.ENGLISH,
                "Can not query service list from service center! %s", ex.getMessage()));
        }
        return serviceList;
    }

    private Optional<String> getMicroserviceName(Microservice microservice) {
        final RegisterConfig pluginConfig = PluginConfigManager.getPluginConfig(RegisterConfig.class);
        if (!environmentEqual(microservice, pluginConfig)) {
            return Optional.empty();
        }
        if (microservice.getAppId().equals(pluginConfig.getApplication())) {
            return Optional.of(microservice.getServiceName());
        } else if (pluginConfig.isAllowCrossApp()) {
            // 当允许跨app时, 则可采用appId.serviceName别名方式返回
            return Optional.of(String.format(Locale.ENGLISH, "%s.%s", microservice.getAppId(),
                microservice.getServiceName()));
        } else {
            return Optional.empty();
        }
    }

    private boolean environmentEqual(Microservice microservice, RegisterConfig pluginConfig) {
        // empty is equal.
        if (StringUtils.isBlank(microservice.getEnvironment()) && StringUtils.isBlank(pluginConfig.getEnvironment())) {
            return true;
        }
        return StringUtils.equals(microservice.getEnvironment(), pluginConfig.getEnvironment());
    }

    @Override
    public RegisterType registerType() {
        return RegisterType.SERVICE_COMB;
    }

    private List<MicroserviceInstance> getScInstances(String serviceName) {
        if (serviceName == null) {
            return Collections.emptyList();
        }
        final List<MicroserviceInstance> microserviceInstances = client.queryInstancesByServiceId(serviceName);
        if (microserviceInstances == null) {
            return Collections.emptyList();
        }
        microserviceInstances.removeIf(next -> next.getStatus() != MicroserviceInstanceStatus.UP);
        return microserviceInstances;
    }
}
