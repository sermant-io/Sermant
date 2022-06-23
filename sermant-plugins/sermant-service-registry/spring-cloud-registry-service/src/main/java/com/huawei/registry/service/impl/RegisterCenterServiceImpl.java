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

package com.huawei.registry.service.impl;

import com.huawei.registry.config.RegisterConfig;
import com.huawei.registry.entity.FixedResult;
import com.huawei.registry.entity.MicroServiceInstance;
import com.huawei.registry.service.register.RegisterManager;
import com.huawei.registry.services.RegisterCenterService;

import com.huaweicloud.sermant.core.common.LoggerFactory;
import com.huaweicloud.sermant.core.plugin.config.PluginConfigManager;

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
        if (getRegisterConfig().isEnableSpringRegister()) {
            RegisterManager.INSTANCE.start();
        }
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
    public void unRegister() {
        RegisterManager.INSTANCE.stop();
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

    @Override
    public List<String> getServices() {
        return RegisterManager.INSTANCE.getServices();
    }

    @Override
    public String getRegisterCenterStatus() {
        return RegisterManager.INSTANCE.getRegister().getRegisterCenterStatus();
    }

    @Override
    public String getInstanceStatus() {
        return RegisterManager.INSTANCE.getRegister().getInstanceStatus();
    }

    @Override
    public void updateInstanceStatus(String status) {
        RegisterManager.INSTANCE.getRegister().updateInstanceStatus(status);
    }

    private RegisterConfig getRegisterConfig() {
        if (registerConfig == null) {
            registerConfig = PluginConfigManager.getPluginConfig(RegisterConfig.class);
        }
        return registerConfig;
    }
}
