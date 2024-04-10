/*
 * Copyright (C) 2021-2021 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huawei.registry.interceptors;

import com.huawei.registry.context.RegisterContext;
import com.huawei.registry.support.RegisterSwitchSupport;

import com.huaweicloud.sermant.core.plugin.agent.entity.ExecuteContext;

import org.springframework.cloud.client.discovery.composite.CompositeDiscoveryClient;

/**
 * Intercept to get a list of services
 *
 * @author zhouss
 * @since 2021-12-13
 */
public class ClientConfigurationInterceptor extends RegisterSwitchSupport {
    @Override
    public ExecuteContext doAfter(ExecuteContext context) {
        final Object result = context.getResult();
        if (result instanceof CompositeDiscoveryClient) {
            RegisterContext.INSTANCE.setDiscoveryClient(result);
        }
        return context;
    }
}
