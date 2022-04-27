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

import com.huawei.loadbalancer.cache.LoadbalancerCache;

import com.huaweicloud.sermant.core.plugin.agent.entity.ExecuteContext;
import com.huaweicloud.sermant.core.plugin.agent.interceptor.AbstractInterceptor;

/**
 * LoadBalancer增强类
 *
 * @author provenceee
 * @since 2022-01-20
 */
public class LoadBalancerInterceptor extends AbstractInterceptor {
    private static final int EXPECT_LENGTH = 2;

    @Override
    public ExecuteContext before(ExecuteContext context) {
        return context;
    }

    @Override
    public ExecuteContext after(ExecuteContext context) {
        saveLoadBalancerArgs(context.getObject(), context.getArguments());
        return context;
    }

    private void saveLoadBalancerArgs(Object obj, Object[] arguments) {
        if (obj == null || arguments == null) {
            return;
        }
        if (arguments.length < EXPECT_LENGTH || arguments[0] == null || arguments[1] == null) {
            return;
        }
        String serviceId = (String) arguments[1];
        LoadbalancerCache.INSTANCE.putProvider(serviceId, arguments[0]);
        LoadbalancerCache.INSTANCE.putOrigin(serviceId, obj);
    }
}