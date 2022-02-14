/*
 * Copyright (C) 2021-2022 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huawei.dubbo.register.service;

import com.huawei.dubbo.register.constants.Constant;
import com.huawei.dubbo.register.utils.CollectionUtils;
import com.huawei.dubbo.register.utils.ReflectUtils;
import com.huawei.register.config.RegisterConfig;
import com.huawei.sermant.core.plugin.config.PluginConfigManager;

import java.util.List;

/**
 * 注册配置服务，代码中使用反射调用类方法是为了同时兼容alibaba和apache dubbo
 *
 * @author provenceee
 * @since 2021/12/31
 */
public class RegistryConfigServiceImpl implements RegistryConfigService {
    private static final String DUBBO_REGISTRIES_CONFIG_PREFIX = "dubbo.registries.";

    private final RegisterConfig config;

    public RegistryConfigServiceImpl() {
        config = PluginConfigManager.getPluginConfig(RegisterConfig.class);
    }

    /**
     * 多注册中心注册到sc
     *
     * @param obj 增强的类
     * @see com.alibaba.dubbo.config.AbstractInterfaceConfig
     * @see org.apache.dubbo.config.AbstractInterfaceConfig
     */
    @Override
    public void addRegistryConfig(Object obj) {
        if (!config.isOpenMigration()) {
            return;
        }
        List<Object> registries = ReflectUtils.getRegistries(obj);
        if (CollectionUtils.isEmpty(registries) || isInValid(registries)) {
            return;
        }
        Class<?> clazz = registries.get(0).getClass();
        Object registryConfig = ReflectUtils.newRegistryConfig(clazz);
        if (registryConfig == null) {
            return;
        }
        ReflectUtils.setId(registryConfig, Constant.SC_REGISTRY_PROTOCOL);
        ReflectUtils.setPrefix(registryConfig, DUBBO_REGISTRIES_CONFIG_PREFIX);
        registries.add(registryConfig);
    }

    @SuppressWarnings("checkstyle:RegexpSingleline")
    private boolean isInValid(List<?> registries) {
        // 是否所有的配置都是无效的
        boolean allRegistriesAreInvalid = true;
        for (Object registry : registries) {
            if (registry == null) {
                continue;
            }
            if (ReflectUtils.isValid(registry)) {
                allRegistriesAreInvalid = false;
            }
            if (Constant.SC_REGISTRY_PROTOCOL.equals(ReflectUtils.getId(registry))
                || Constant.SC_REGISTRY_PROTOCOL.equals(ReflectUtils.getProtocol(registry))) {
                // 如果存在sc的配置，直接return，不注册到sc
                return true;
            }
        }

        // 如果所有的配置都是无效的，则为无效配置，不注册到sc
        return allRegistriesAreInvalid;
    }
}