/*
 * Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package io.sermant.registry.auto.sc;

import io.sermant.core.common.LoggerFactory;
import io.sermant.core.plugin.config.PluginConfigManager;
import io.sermant.core.plugin.service.PluginServiceManager;
import io.sermant.registry.config.GraceConfig;
import io.sermant.registry.config.RegisterServiceCommonConfig;
import io.sermant.registry.config.grace.GraceHelper;
import io.sermant.registry.context.RegisterContext;
import io.sermant.registry.entity.FixedResult;
import io.sermant.registry.services.RegisterCenterService;
import io.sermant.registry.utils.CommonUtils;
import io.sermant.registry.utils.ZoneUtils;

import org.springframework.cloud.client.serviceregistry.Registration;
import org.springframework.cloud.client.serviceregistry.ServiceRegistry;

import java.util.logging.Logger;

/**
 * ServiceComb registration
 *
 * @author zhouss
 * @since 2022-05-18
 */
public class ServiceCombRegistry implements ServiceRegistry<ServiceCombRegistration> {
    private static final Logger LOGGER = LoggerFactory.getLogger();

    private RegisterCenterService registerCenterService;

    @Override
    public void register(ServiceCombRegistration registration) {
        fillClientInfo(registration);
        GraceHelper.configWarmUpParams(registration.getMetadata(),
                PluginConfigManager.getPluginConfig(GraceConfig.class));
        ZoneUtils.setZone(registration.getMetadata());
        RegisterCenterService registerService = getRegisterCenterService();
        if (registerService == null) {
            LOGGER.severe("registerCenterService is null, fail to register!");
            return;
        }
        registerService.register(new FixedResult());
    }

    @Override
    public void deregister(ServiceCombRegistration registration) {
        RegisterCenterService registerService = getRegisterCenterService();
        if (registerService == null) {
            LOGGER.severe("registerCenterService is null, fail to unRegister!");
            return;
        }
        registerService.unRegister();
    }

    @Override
    public void close() {
        LOGGER.warning("Service Comb has been closed!");
    }

    @Override
    public void setStatus(ServiceCombRegistration registration, String status) {
        RegisterCenterService registerService = getRegisterCenterService();
        if (registerService == null) {
            LOGGER.severe("registerCenterService is null, fail to update instance status!");
            return;
        }
        registerService.updateInstanceStatus(status);
    }

    @Override
    public String getStatus(ServiceCombRegistration registration) {
        return getRegisterCenterService().getInstanceStatus();
    }

    private RegisterCenterService getRegisterCenterService() {
        if (registerCenterService == null) {
            try {
                registerCenterService = PluginServiceManager.getPluginService(RegisterCenterService.class);
            } catch (IllegalArgumentException e) {
                LOGGER.severe("registerCenterService is not enabled!");
            }
        }
        return registerCenterService;
    }

    private void fillClientInfo(Registration registration) {
        RegisterContext.INSTANCE.getClientInfo().setHost(registration.getHost());
        RegisterServiceCommonConfig config = PluginConfigManager.getPluginConfig(RegisterServiceCommonConfig.class);
        RegisterContext.INSTANCE.getClientInfo().setMeta(CommonUtils.putSecureToMetaData(registration.getMetadata(),
            config));
        RegisterContext.INSTANCE.getClientInfo().setPort(registration.getPort());
        RegisterContext.INSTANCE.getClientInfo().setServiceId(registration.getServiceId());
    }
}
