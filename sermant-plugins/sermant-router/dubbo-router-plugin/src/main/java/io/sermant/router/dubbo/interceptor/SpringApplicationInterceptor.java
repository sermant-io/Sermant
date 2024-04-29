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

package io.sermant.router.dubbo.interceptor;

import io.sermant.core.plugin.agent.entity.ExecuteContext;
import io.sermant.core.plugin.agent.interceptor.AbstractInterceptor;
import io.sermant.core.plugin.service.PluginServiceManager;
import io.sermant.router.common.cache.DubboCache;
import io.sermant.router.common.constants.RouterConstant;
import io.sermant.router.dubbo.service.DubboConfigService;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Enhance the run method of the SpringApplication class
 *
 * @author provenceee
 * @since 2022-01-24
 */
public class SpringApplicationInterceptor extends AbstractInterceptor {
    private static final AtomicBoolean INIT = new AtomicBoolean();

    private final DubboConfigService configService;

    /**
     * Constructor
     */
    public SpringApplicationInterceptor() {
        configService = PluginServiceManager.getPluginService(DubboConfigService.class);
    }

    @Override
    public ExecuteContext before(ExecuteContext context) {
        return context;
    }

    @Override
    public ExecuteContext after(ExecuteContext context) {
        Object logStartupInfo = context.getMemberFieldValue("logStartupInfo");
        if ((logStartupInfo instanceof Boolean) && (Boolean) logStartupInfo && INIT.compareAndSet(false, true)) {
            configService.init(RouterConstant.DUBBO_CACHE_NAME, DubboCache.INSTANCE.getAppName());
        }
        return context;
    }
}