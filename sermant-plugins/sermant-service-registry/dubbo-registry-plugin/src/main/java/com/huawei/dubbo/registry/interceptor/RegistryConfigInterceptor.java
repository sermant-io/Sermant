/*
 * Copyright (C) 2022-2024 Sermant Authors. All rights reserved.
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

package com.huawei.dubbo.registry.interceptor;

import com.huawei.dubbo.registry.constants.Constant;
import com.huawei.registry.config.RegisterConfig;

import com.huaweicloud.sermant.core.plugin.agent.entity.ExecuteContext;
import com.huaweicloud.sermant.core.plugin.agent.interceptor.AbstractInterceptor;
import com.huaweicloud.sermant.core.plugin.config.PluginConfigManager;

/**
 * Enhance the setProtocol/setAddress method of the RegistryConfiguration class
 *
 * @author provenceee
 * @since 2022-04-13
 */
public class RegistryConfigInterceptor extends AbstractInterceptor {
    private final RegisterConfig config;

    /**
     * Constructor
     */
    public RegistryConfigInterceptor() {
        config = PluginConfigManager.getPluginConfig(RegisterConfig.class);
    }

    @Override
    public ExecuteContext before(ExecuteContext context) {
        if (config.isEnableDubboRegister() && !config.isOpenMigration()) {
            context.getArguments()[0] = Constant.SET_PROTOCOL_METHOD_NAME.equals(context.getMethod().getName())
                ? Constant.SC_REGISTRY_PROTOCOL : Constant.SC_REGISTRY_ADDRESS;
        }
        return context;
    }

    @Override
    public ExecuteContext after(ExecuteContext context) {
        return context;
    }
}