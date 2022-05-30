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

package com.huawei.registry.auto.sc;

import com.huawei.registry.config.GraceConfig;
import com.huawei.registry.config.grace.GraceHelper;
import com.huawei.registry.context.RegisterContext;
import com.huawei.registry.entity.FixedResult;
import com.huawei.registry.services.RegisterCenterService;
import com.huawei.registry.utils.ZoneUtils;

import com.huaweicloud.sermant.core.common.LoggerFactory;
import com.huaweicloud.sermant.core.plugin.config.PluginConfigManager;
import com.huaweicloud.sermant.core.plugin.service.PluginServiceManager;

import org.springframework.cloud.client.serviceregistry.Registration;
import org.springframework.cloud.client.serviceregistry.ServiceRegistry;

import java.util.logging.Logger;

/**
 * ServiceComb注册
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
        getRegisterCenterService().register(new FixedResult());
    }

    @Override
    public void deregister(ServiceCombRegistration registration) {
        getRegisterCenterService().unRegister();
    }

    @Override
    public void close() {
        LOGGER.warning("Service Comb has been closed!");
    }

    @Override
    public void setStatus(ServiceCombRegistration registration, String status) {
        getRegisterCenterService().updateInstanceStatus(status);
    }

    @Override
    public String getStatus(ServiceCombRegistration registration) {
        return getRegisterCenterService().getInstanceStatus();
    }

    private RegisterCenterService getRegisterCenterService() {
        if (registerCenterService == null) {
            registerCenterService = PluginServiceManager.getPluginService(RegisterCenterService.class);
        }
        return registerCenterService;
    }

    private void fillClientInfo(Registration registration) {
        RegisterContext.INSTANCE.getClientInfo().setHost(registration.getHost());
        RegisterContext.INSTANCE.getClientInfo().setMeta(registration.getMetadata());
        RegisterContext.INSTANCE.getClientInfo().setPort(registration.getPort());
        RegisterContext.INSTANCE.getClientInfo().setServiceId(registration.getServiceId());
    }
}
