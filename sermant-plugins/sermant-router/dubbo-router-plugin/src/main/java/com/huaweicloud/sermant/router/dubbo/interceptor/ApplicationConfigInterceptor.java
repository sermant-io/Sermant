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

package com.huaweicloud.sermant.router.dubbo.interceptor;

import com.huaweicloud.sermant.core.plugin.agent.entity.ExecuteContext;
import com.huaweicloud.sermant.core.plugin.agent.interceptor.AbstractInterceptor;
import com.huaweicloud.sermant.core.plugin.config.PluginConfigManager;
import com.huaweicloud.sermant.core.utils.StringUtils;
import com.huaweicloud.sermant.router.common.cache.DubboCache;
import com.huaweicloud.sermant.router.common.config.RouterConfig;
import com.huaweicloud.sermant.router.common.utils.DubboReflectUtils;
import com.huaweicloud.sermant.router.dubbo.utils.ParametersUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * 增强ApplicationConfig类的setName方法，用来获取应用名
 *
 * @author provenceee
 * @since 2021-11-08
 */
public class ApplicationConfigInterceptor extends AbstractInterceptor {
    private final RouterConfig routerConfig;

    /**
     * 构造方法
     */
    public ApplicationConfigInterceptor() {
        routerConfig = PluginConfigManager.getPluginConfig(RouterConfig.class);
    }

    @Override
    public ExecuteContext before(ExecuteContext context) {
        String name = context.getMethod().getName();
        if (context.getArguments() == null || context.getArguments().length == 0) {
            return context;
        }
        Object argument = context.getArguments()[0];
        if ("setName".equals(name)) {
            if (argument == null || argument instanceof String) {
                setAppNameAndPutParameters(context.getObject(), (String) argument);
            }
        } else {
            if (argument == null || argument instanceof Map<?, ?>) {
                context.getArguments()[0] = ParametersUtils.putParameters((Map<String, String>) argument,
                        routerConfig);
            }
        }
        return context;
    }

    @Override
    public ExecuteContext after(ExecuteContext context) {
        return context;
    }

    private void setAppNameAndPutParameters(Object config, String name) {
        if (StringUtils.isBlank(name)) {
            return;
        }
        DubboCache.INSTANCE.setAppName(name);
        DubboReflectUtils.setParameters(config, new HashMap<>());
    }
}
