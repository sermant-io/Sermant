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

package com.huaweicloud.sermant.router.spring.interceptor;

import com.huaweicloud.sermant.core.common.LoggerFactory;
import com.huaweicloud.sermant.core.plugin.agent.entity.ExecuteContext;
import com.huaweicloud.sermant.core.plugin.agent.interceptor.AbstractInterceptor;
import com.huaweicloud.sermant.core.plugin.config.PluginConfigManager;
import com.huaweicloud.sermant.core.service.ServiceManager;
import com.huaweicloud.sermant.core.utils.ReflectUtils;
import com.huaweicloud.sermant.router.common.config.RouterConfig;
import com.huaweicloud.sermant.router.common.constants.RouterConstant;
import com.huaweicloud.sermant.router.spring.cache.AppCache;
import com.huaweicloud.sermant.router.spring.service.SpringConfigService;
import com.huaweicloud.sermant.router.spring.utils.SpringRouterUtils;

import java.util.logging.Logger;

/**
 * 注册插件拦截点
 *
 * @author provenceee
 * @since 2022-10-13
 */
public class DiscoveryManagerInterceptor extends AbstractInterceptor {
    private static final Logger LOGGER = LoggerFactory.getLogger();

    private final SpringConfigService configService;

    private final RouterConfig routerConfig;

    /**
     * 构造方法
     */
    public DiscoveryManagerInterceptor() {
        configService = ServiceManager.getService(SpringConfigService.class);
        routerConfig = PluginConfigManager.getPluginConfig(RouterConfig.class);
    }

    @Override
    public ExecuteContext before(ExecuteContext context) {
        Object[] arguments = context.getArguments();
        if (arguments != null && arguments.length > 0) {
            Object obj = arguments[0];
            Object serviceName = ReflectUtils.getFieldValue(obj, "serviceName").orElse(null);
            if (serviceName instanceof String) {
                AppCache.INSTANCE.setAppName((String) serviceName);
            } else {
                LOGGER.warning("Service name is null or not instanceof string.");
            }
            SpringRouterUtils.putMetaData(SpringRouterUtils.getMetadata(obj), routerConfig);
        }
        return context;
    }

    @Override
    public ExecuteContext after(ExecuteContext context) {
        configService.init(RouterConstant.SPRING_CACHE_NAME, AppCache.INSTANCE.getAppName());
        return context;
    }
}