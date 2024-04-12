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

import com.huawei.discovery.service.RegistryService;

import com.huaweicloud.sermant.core.plugin.agent.entity.ExecuteContext;
import com.huaweicloud.sermant.core.plugin.agent.interceptor.AbstractInterceptor;
import com.huaweicloud.sermant.core.plugin.service.PluginServiceManager;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.event.ContextClosedEvent;

/**
 * Spring disables event listeners{@link org.springframework.context.event.ContextClosedEvent}
 *
 * @author zhouss
 * @since 2022-11-16
 */
public class SpringCloseEventInterceptor extends AbstractInterceptor {
    private final RegistryService registryService;

    /**
     * Constructor
     */
    public SpringCloseEventInterceptor() {
        registryService = PluginServiceManager.getPluginService(RegistryService.class);
    }

    @Override
    public ExecuteContext before(ExecuteContext context) throws Exception {
        final Object rawEvent = context.getArguments()[0];
        if (rawEvent instanceof ContextClosedEvent) {
            tryShutdown((ContextClosedEvent) rawEvent);
        }
        return context;
    }

    private void tryShutdown(ContextClosedEvent event) {
        if (event.getSource() instanceof AnnotationConfigApplicationContext) {
            // This type is a refresh event and is not handled
            return;
        }
        registryService.shutdown();
    }

    @Override
    public ExecuteContext after(ExecuteContext context) throws Exception {
        return context;
    }
}
