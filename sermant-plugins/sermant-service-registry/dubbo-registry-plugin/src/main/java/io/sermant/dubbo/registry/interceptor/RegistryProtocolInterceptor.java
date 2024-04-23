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

package io.sermant.dubbo.registry.interceptor;

import io.sermant.core.plugin.agent.entity.ExecuteContext;
import io.sermant.core.plugin.agent.interceptor.AbstractInterceptor;
import io.sermant.dubbo.registry.constants.Constant;

import org.apache.dubbo.common.URL;

/**
 * Enhance the getServiceDiscoveryInvoker method of the InterfaceCompatibleRegistryProtocol class
 *
 * @author provenceee
 * @since 2022-01-26
 */
public class RegistryProtocolInterceptor extends AbstractInterceptor {
    private static final int URL_INDEX = 2;

    /**
     * This method is to prevent 2.7.9 from loading sc ServiceDiscovery
     *
     * @param context Execution context
     * @return Execution context
     */
    @Override
    public ExecuteContext before(ExecuteContext context) {
        Object[] arguments = context.getArguments();
        if (arguments != null && arguments.length > URL_INDEX && arguments[URL_INDEX] instanceof URL) {
            if (Constant.SC_REGISTRY_PROTOCOL.equals(((URL) arguments[URL_INDEX]).getProtocol())) {
                // The registration of the sc protocol can be returned directly, so that the sc ServiceDiscovery cannot
                // be loaded, that is, the 2.7.9 sc application-level registration can be blocked
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
