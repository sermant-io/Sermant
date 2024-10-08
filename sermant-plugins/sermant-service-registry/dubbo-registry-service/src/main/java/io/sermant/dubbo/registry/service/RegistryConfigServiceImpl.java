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

package io.sermant.dubbo.registry.service;

import io.sermant.core.plugin.config.PluginConfigManager;
import io.sermant.dubbo.registry.constants.Constant;
import io.sermant.dubbo.registry.utils.CollectionUtils;
import io.sermant.dubbo.registry.utils.ReflectUtils;
import io.sermant.registry.config.RegisterConfig;

import java.util.List;
import java.util.Optional;

/**
 * To register a configuration service, the code uses reflection to call class methods to be compatible with both
 * Alibaba and Apache Dubbo
 *
 * @author provenceee
 * @since 2021-12-31
 */
public class RegistryConfigServiceImpl implements RegistryConfigService {
    private static final String DUBBO_REGISTRIES_CONFIG_PREFIX = "dubbo.registries.";

    private final RegisterConfig config;

    /**
     * Constructor
     */
    public RegistryConfigServiceImpl() {
        config = PluginConfigManager.getPluginConfig(RegisterConfig.class);
    }

    /**
     * Multi-registry registration to SC
     *
     * @param obj Enhanced classes
     * @see com.alibaba.dubbo.config.AbstractInterfaceConfig
     * @see org.apache.dubbo.config.AbstractInterfaceConfig
     */
    @Override
    public void addRegistryConfig(Object obj) {
        if (!config.isOpenMigration() || !config.isEnableDubboRegister()) {
            return;
        }
        List<Object> registries = ReflectUtils.getRegistries(obj);
        if (CollectionUtils.isEmpty(registries) || isInValid(registries)) {
            return;
        }
        Class<?> clazz = registries.get(0).getClass();
        Optional<?> registryConfig = ReflectUtils.newRegistryConfig(clazz);
        if (!registryConfig.isPresent()) {
            return;
        }
        ReflectUtils.setId(registryConfig.get(), Constant.SC_REGISTRY_PROTOCOL);
        ReflectUtils.setPrefix(registryConfig.get(), DUBBO_REGISTRIES_CONFIG_PREFIX);
        registries.add(registryConfig.get());
    }

    private boolean isInValid(List<?> registries) {
        // Whether all configurations are invalid
        boolean isInvalid = true;
        for (Object registry : registries) {
            if (registry == null) {
                continue;
            }
            if (ReflectUtils.isValid(registry)) {
                isInvalid = false;
            }
            if (Constant.SC_REGISTRY_PROTOCOL.equals(ReflectUtils.getId(registry))
                    || Constant.SC_REGISTRY_PROTOCOL.equals(ReflectUtils.getProtocol(registry))) {
                // If there is an SC configuration, return directly and do not register with the SC
                return true;
            }
        }

        // If all configurations are invalid, they are invalid and are not registered with the SC
        return isInvalid;
    }
}
