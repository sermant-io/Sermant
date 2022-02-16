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

package com.huawei.register.service.impl;

import com.huawei.register.config.RegisterConfig;
import com.huawei.register.context.RegisterContext;
import com.huawei.register.entity.FixedResult;
import com.huawei.register.entity.MicroServiceInstance;
import com.huawei.register.service.register.RegisterManager;
import com.huawei.register.service.utils.CommonUtils;
import com.huawei.register.services.RegisterCenterService;
import com.huawei.sermant.core.common.LoggerFactory;
import com.huawei.sermant.core.plugin.config.PluginConfigManager;

import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

/**
 * 注册实现
 *
 * @author zhouss
 * @since 2021-12-16
 */
public class RegisterCenterServiceImpl implements RegisterCenterService {
    private static final Logger LOGGER = LoggerFactory.getLogger();

    private RegisterConfig registerConfig;

    @Override
    public void start() {
        RegisterManager.INSTANCE.start();
    }

    @Override
    public void stop() {
        RegisterManager.INSTANCE.stop();
    }

    @Override
    public void register(FixedResult result) {
        RegisterManager.INSTANCE.register();
        if (!getRegisterConfig().isOpenMigration()) {
            // 阻止原注册中心注册
            result.setResult(null);
        }
    }

    @Override
    public List<MicroServiceInstance> getServerList(Object target) {
        String serviceId = (String) CommonUtils.getFieldValue(target, "serviceId");
        if (serviceId == null && RegisterContext.INSTANCE.getClientInfo().getServiceName() != null) {
            // 若未获取到服务名，则从注册基本信息获取
            serviceId = RegisterContext.INSTANCE.getClientInfo().getServiceName();
        }
        return getServerList(serviceId);
    }

    @Override
    public List<MicroServiceInstance> getServerList(String serviceId) {
        if (serviceId == null) {
            // 无法执行替换
            LOGGER.warning("Can not acquire the name of service, the process to replace instance won't be finished!");
            return Collections.emptyList();
        }
        return RegisterManager.INSTANCE.getServerList(serviceId);
    }

    private RegisterConfig getRegisterConfig() {
        if (registerConfig == null) {
            registerConfig = PluginConfigManager.getPluginConfig(RegisterConfig.class);
        }
        return registerConfig;
    }
}
