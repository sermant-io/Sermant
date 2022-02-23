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

package com.huawei.loadbalancer.interceptor;

import com.huawei.loadbalancer.config.LoadbalancerConfig;
import com.huawei.sermant.core.plugin.agent.entity.ExecuteContext;
import com.huawei.sermant.core.plugin.agent.interceptor.AbstractInterceptor;
import com.huawei.sermant.core.plugin.config.PluginConfigManager;

import java.util.Locale;

/**
 * URL增强类
 *
 * @author provenceee
 * @since 2022/1/20
 */
public class UrlInterceptor extends AbstractInterceptor {
    private static final String LOAD_BALANCE_KEY = "loadbalance";

    private final LoadbalancerConfig config;

    public UrlInterceptor() {
        config = PluginConfigManager.getPluginConfig(LoadbalancerConfig.class);
    }

    @Override
    public ExecuteContext before(ExecuteContext context) {
        Object[] arguments = context.getArguments();
        if (arguments != null && arguments.length > 1 && LOAD_BALANCE_KEY.equals(arguments[1])) {
            String type = getType();

            // 如果为null，继续执行原方法，即使用宿主的负载均衡策略
            // 如果不为null，则使用返回的type并跳过原方法
            if (type != null) {
                context.skip(type);
            }
        }
        return context;
    }

    @Override
    public ExecuteContext after(ExecuteContext context) {
        return context;
    }

    private String getType() {
        if (config == null || config.getDubboType() == null) {
            // 没有配置的情况下return null
            return null;
        }
        return config.getDubboType().name().toLowerCase(Locale.ROOT);
    }
}