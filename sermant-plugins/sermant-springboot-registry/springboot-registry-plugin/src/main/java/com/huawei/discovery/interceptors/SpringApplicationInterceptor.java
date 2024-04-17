/*
 * Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huawei.discovery.interceptors;

import com.huawei.discovery.entity.RegisterContext;
import com.huawei.discovery.event.SpringBootRegistryEventCollector;
import com.huawei.discovery.service.ConfigCenterService;
import com.huawei.discovery.service.RegistryService;

import com.huaweicloud.sermant.core.plugin.agent.entity.ExecuteContext;
import com.huaweicloud.sermant.core.plugin.agent.interceptor.AbstractInterceptor;
import com.huaweicloud.sermant.core.plugin.service.PluginServiceManager;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * The end phase starts registering microservices
 *
 * @author chengyouling
 * @since 2022-10-10
 */
public class SpringApplicationInterceptor extends AbstractInterceptor {
    private static final AtomicBoolean INIT = new AtomicBoolean();

    private final RegistryService registryService;

    private final ConfigCenterService configCenterService;

    /**
     * Constructor
     */
    public SpringApplicationInterceptor() {
        registryService = PluginServiceManager.getPluginService(RegistryService.class);
        configCenterService = PluginServiceManager.getPluginService(ConfigCenterService.class);
    }

    @Override
    public ExecuteContext before(ExecuteContext context) throws Exception {
        return context;
    }

    @Override
    public ExecuteContext after(ExecuteContext context) {
        Object logStartupInfo = context.getMemberFieldValue("logStartupInfo");
        if ((logStartupInfo instanceof Boolean) && (Boolean) logStartupInfo && INIT.compareAndSet(false, true)) {
            registryService.registry(RegisterContext.INSTANCE.getServiceInstance());
            configCenterService.init(RegisterContext.INSTANCE.getServiceInstance().getServiceName());
            SpringBootRegistryEventCollector.getInstance().collectRegistryEvent(RegisterContext.INSTANCE
                    .getServiceInstance());
        }
        return context;
    }
}