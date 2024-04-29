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

package io.sermant.dubbo.registry.declarer;

import io.sermant.core.plugin.agent.declarer.InterceptDeclarer;
import io.sermant.core.plugin.agent.matcher.MethodMatcher;

import net.bytebuddy.matcher.ElementMatchers;

/**
 * InterfaceCompatibleRegistryProtocol Enhancement Classes
 *
 * @author provenceee
 * @since 2022-01-26
 */
public class RegistryProtocolDeclarer extends AbstractDeclarer {
    private static final String[] ENHANCE_CLASS
            = {"org.apache.dubbo.registry.integration.InterfaceCompatibleRegistryProtocol"};

    private static final String INTERCEPT_CLASS = "io.sermant.dubbo.registry.interceptor.RegistryProtocolInterceptor";

    private static final String METHOD_NAME = "getServiceDiscoveryInvoker";

    /**
     * Constructor
     */
    public RegistryProtocolDeclarer() {
        super(ENHANCE_CLASS);
    }

    @Override
    public InterceptDeclarer[] getInterceptDeclarers(ClassLoader classLoader) {
        return new InterceptDeclarer[]{
                // This method is protected in version 2.7.9, while versions above version 2.7.9 are public,
                // so isProtected is designed to only intercept versions 2.7.9
                InterceptDeclarer.build(MethodMatcher.nameEquals(METHOD_NAME).and(ElementMatchers.isProtected()),
                        INTERCEPT_CLASS)
        };
    }
}
