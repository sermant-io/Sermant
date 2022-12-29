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

package com.huaweicloud.sermant.router.dubbo.interceptor;

import com.huaweicloud.sermant.core.plugin.agent.entity.ExecuteContext;
import com.huaweicloud.sermant.core.plugin.agent.interceptor.AbstractInterceptor;
import com.huaweicloud.sermant.core.plugin.config.PluginConfigManager;
import com.huaweicloud.sermant.router.common.config.RouterConfig;
import com.huaweicloud.sermant.router.dubbo.utils.ParametersUtils;

import com.alibaba.dubbo.config.ApplicationConfig;

import java.util.Map;

/**
 * 增强AbstractConfig类的appendParameters方法，增加路由标签
 *
 * @author chengyouling
 * @since 2022-12-28
 */
public class AbstractConfigInterceptor extends AbstractInterceptor {
    private final RouterConfig routerConfig;

    /**
     * 构造方法
     */
    public AbstractConfigInterceptor() {
        routerConfig = PluginConfigManager.getPluginConfig(RouterConfig.class);
    }

    @Override
    public ExecuteContext before(ExecuteContext context) {
        if (context.getArguments() == null || context.getArguments().length == 0) {
            return context;
        }
        Object parameters = context.getArguments()[0];
        Object config = context.getArguments()[1];
        if (parameters instanceof Map<?, ?> && config instanceof ApplicationConfig) {
            context.getArguments()[0] = ParametersUtils.putParameters((Map<String, String>) parameters,
                routerConfig);
        }
        return context;
    }

    @Override
    public ExecuteContext after(ExecuteContext context) {
        return context;
    }
}
