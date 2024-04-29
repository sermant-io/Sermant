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

package io.sermant.router.spring.interceptor;

import com.netflix.appinfo.InstanceInfo;

import io.sermant.core.plugin.agent.entity.ExecuteContext;
import io.sermant.core.plugin.agent.interceptor.AbstractInterceptor;
import io.sermant.core.plugin.config.PluginConfigManager;
import io.sermant.core.plugin.service.PluginServiceManager;
import io.sermant.router.common.config.RouterConfig;
import io.sermant.router.common.constants.RouterConstant;
import io.sermant.router.spring.cache.AppCache;
import io.sermant.router.spring.service.SpringConfigService;
import io.sermant.router.spring.utils.SpringRouterUtils;

/**
 * EurekaHttpClient Enhancement Class, Eureka Registration Method
 *
 * @author provenceee
 * @since 2022-07-12
 */
public class EurekaHttpClientInterceptor extends AbstractInterceptor {
    private final RouterConfig routerConfig;

    private final SpringConfigService configService;

    /**
     * Constructor
     */
    public EurekaHttpClientInterceptor() {
        routerConfig = PluginConfigManager.getPluginConfig(RouterConfig.class);
        configService = PluginServiceManager.getPluginService(SpringConfigService.class);
    }

    @Override
    public ExecuteContext before(ExecuteContext context) {
        Object argument = context.getArguments()[0];
        if (argument instanceof InstanceInfo) {
            InstanceInfo instanceInfo = (InstanceInfo) argument;
            AppCache.INSTANCE.setAppName(instanceInfo.getAppName());
            SpringRouterUtils.putMetaData(instanceInfo.getMetadata(), routerConfig);
        }
        return context;
    }

    @Override
    public ExecuteContext after(ExecuteContext context) {
        configService.init(RouterConstant.SPRING_CACHE_NAME, AppCache.INSTANCE.getAppName());
        return context;
    }
}