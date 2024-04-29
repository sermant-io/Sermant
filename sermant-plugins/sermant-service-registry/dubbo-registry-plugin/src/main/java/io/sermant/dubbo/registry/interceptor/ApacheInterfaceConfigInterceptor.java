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

package io.sermant.dubbo.registry.interceptor;

import io.sermant.core.plugin.agent.entity.ExecuteContext;
import io.sermant.core.plugin.agent.interceptor.AbstractInterceptor;
import io.sermant.core.plugin.service.PluginServiceManager;
import io.sermant.dubbo.registry.service.RegistryConfigService;

/**
 * Enhance the setRegisteries/loadRegisteriesFromBackwardConfig method of the AbstractInterfaceConfig class
 *
 * @author provenceee
 * @since 2021-11-08
 */
public class ApacheInterfaceConfigInterceptor extends AbstractInterceptor {
    private final RegistryConfigService registryConfigService;

    /**
     * Constructor
     */
    public ApacheInterfaceConfigInterceptor() {
        registryConfigService = PluginServiceManager.getPluginService(RegistryConfigService.class);
    }

    @Override
    public ExecuteContext before(ExecuteContext context) {
        return context;
    }

    @Override
    public ExecuteContext after(ExecuteContext context) {
        registryConfigService.addRegistryConfig(context.getObject());
        return context;
    }
}