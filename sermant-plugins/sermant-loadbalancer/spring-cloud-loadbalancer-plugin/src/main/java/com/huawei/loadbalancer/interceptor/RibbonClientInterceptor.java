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

import com.huaweicloud.loadbalancer.rule.RuleManager;
import com.huaweicloud.loadbalancer.utils.RibbonUtils;
import com.huaweicloud.sermant.core.plugin.agent.entity.ExecuteContext;
import com.huaweicloud.sermant.core.plugin.agent.interceptor.AbstractInterceptor;
import com.huaweicloud.sermant.core.utils.ReflectUtils;

import com.netflix.client.ClientRequest;
import com.netflix.client.config.IClientConfig;
import com.netflix.loadbalancer.reactive.LoadBalancerCommand;

/**
 * Ribbon Client增强, 配置loadbalancerKey
 *
 * @author provenceee
 * @since 2022-01-20
 */
public class RibbonClientInterceptor extends AbstractInterceptor {
    @Override
    public ExecuteContext before(ExecuteContext context) {
        return context;
    }

    @Override
    public ExecuteContext after(ExecuteContext context) {
        if (!RuleManager.INSTANCE.isConfigured()) {
            return context;
        }
        final Object[] arguments = context.getArguments();
        if (!(arguments[0] instanceof ClientRequest) || !(arguments[1] instanceof IClientConfig)) {
            return context;
        }
        final Object result = context.getResult();
        if (!(result instanceof LoadBalancerCommand)) {
            return context;
        }
        ClientRequest request = (ClientRequest) arguments[0];
        IClientConfig clientConfig = (IClientConfig) arguments[1];
        final Object loadBalancerKey = request.getLoadBalancerKey();
        if (loadBalancerKey == null || RibbonUtils.DEFAULT_RIBBON_LOADBALANCER_KEY.equals(loadBalancerKey)) {
            // 仅用户未配置负载均衡key的场景生效
            ReflectUtils.setFieldValue(result, "loadBalancerKey",
                    RibbonUtils.buildLoadbalancerKey(clientConfig.getClientName()));
        }
        return context;
    }
}
