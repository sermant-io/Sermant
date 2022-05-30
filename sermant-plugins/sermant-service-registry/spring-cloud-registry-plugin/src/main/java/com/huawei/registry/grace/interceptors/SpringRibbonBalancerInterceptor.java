/*
 * Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.huawei.registry.grace.interceptors;

import com.huawei.registry.config.grace.GraceContext;

import com.huaweicloud.sermant.core.plugin.agent.entity.ExecuteContext;

import com.netflix.loadbalancer.DynamicServerListLoadBalancer;

/**
 * ribbon负载均衡拦截
 *
 * @author zhouss
 * @since 2022-05-23
 */
public class SpringRibbonBalancerInterceptor extends GraceSwitchInterceptor {
    @Override
    protected ExecuteContext doBefore(ExecuteContext context) {
        final Object balancer = context.getObject();
        if (balancer instanceof DynamicServerListLoadBalancer) {
            DynamicServerListLoadBalancer<?> dynamicServerListLoadBalancer =
                    (DynamicServerListLoadBalancer<?>) balancer;
            GraceContext.INSTANCE.getGraceShutDownManager()
                    .getLoadBalancerCache().put(dynamicServerListLoadBalancer.getName(), balancer);
        }
        return context;
    }

    @Override
    protected boolean isEnabled() {
        return super.isEnabled() && graceConfig.isEnableGraceShutdown();
    }
}
