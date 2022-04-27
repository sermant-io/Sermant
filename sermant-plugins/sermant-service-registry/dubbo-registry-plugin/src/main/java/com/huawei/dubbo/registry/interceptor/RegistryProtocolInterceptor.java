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

package com.huawei.dubbo.registry.interceptor;

import com.huawei.dubbo.registry.constants.Constant;

import com.huaweicloud.sermant.core.plugin.agent.entity.ExecuteContext;
import com.huaweicloud.sermant.core.plugin.agent.interceptor.AbstractInterceptor;

import org.apache.dubbo.common.URL;

/**
 * 增强InterfaceCompatibleRegistryProtocol类的getServiceDiscoveryInvoker方法
 *
 * @author provenceee
 * @since 2022-01-26
 */
public class RegistryProtocolInterceptor extends AbstractInterceptor {
    private static final int URL_INDEX = 2;

    /**
     * 这个方法是为了让2.7.9不去加载sc ServiceDiscovery
     *
     * @param context 执行上下文
     * @return 执行上下文
     */
    @Override
    public ExecuteContext before(ExecuteContext context) {
        Object[] arguments = context.getArguments();
        if (arguments != null && arguments.length > URL_INDEX && arguments[URL_INDEX] instanceof URL) {
            if (Constant.SC_REGISTRY_PROTOCOL.equals(((URL) arguments[URL_INDEX]).getProtocol())) {
                // sc协议的注册，直接return，这样就可以不去加载sc ServiceDiscovery，即屏蔽2.7.9 sc应用级注册
                context.skip(null);
            }
        }
        return context;
    }

    @Override
    public ExecuteContext after(ExecuteContext context) {
        return context;
    }
}
