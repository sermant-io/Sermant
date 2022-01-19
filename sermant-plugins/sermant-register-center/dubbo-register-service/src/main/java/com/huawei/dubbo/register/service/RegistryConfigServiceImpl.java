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

import com.huawei.dubbo.register.config.DubboCache;

import org.apache.dubbo.common.URL;
import org.apache.dubbo.config.AbstractInterfaceConfig;
import org.apache.dubbo.config.RegistryConfig;

import java.util.List;

/**
 * 注册配置服务
 *
 * @author provenceee
 * @date 2021/12/31
 */
public class RegistryConfigServiceImpl implements RegistryConfigService {
    private static final String SC_REGISTRY_PROTOCOL = "sc";

    private static final String DUBBO_REGISTRIES_CONFIG_PREFIX = "dubbo.registries.";

    /**
     * 多注册中心注册到sc
     *
     * @param obj 增强的类
     */
    @Override
    public void addRegistryConfig(Object obj) {
        if (obj instanceof AbstractInterfaceConfig && DubboCache.INSTANCE.getDubboConfig().isOpenMigration()) {
            AbstractInterfaceConfig config = (AbstractInterfaceConfig) obj;
            List<RegistryConfig> registries = config.getRegistries();
            if (registries == null || isInValid(registries)) {
                return;
            }
            // 这个url不重要，重要的是protocol，所以get(0)就行
            URL url = URL.valueOf(DubboCache.INSTANCE.getDubboConfig().getAddress().get(0))
                    .setProtocol(SC_REGISTRY_PROTOCOL);
            RegistryConfig registryConfig = new RegistryConfig(url.toString());
            registryConfig.setId(SC_REGISTRY_PROTOCOL);
            registryConfig.setPrefix(DUBBO_REGISTRIES_CONFIG_PREFIX);
            registries.add(registryConfig);
        }
    }

    private boolean isInValid(List<RegistryConfig> registries) {
        // 是否所有的配置都是无效的
        boolean allRegistriesAreInvalid = true;
        // 是否存在sc的注册配置
        boolean hasScRegistryConfig = false;
        for (RegistryConfig registry : registries) {
            if (registry == null) {
                continue;
            }
            if (registry.isValid()) {
                allRegistriesAreInvalid = false;
            }
            if (SC_REGISTRY_PROTOCOL.equals(registry.getId()) || SC_REGISTRY_PROTOCOL.equals(registry.getProtocol())) {
                hasScRegistryConfig = true;
            }
        }
        // 如果所有的配置都是无效的或者存在sc的配置，都属于无效配置，不注册到sc
        return allRegistriesAreInvalid || hasScRegistryConfig;
    }
}