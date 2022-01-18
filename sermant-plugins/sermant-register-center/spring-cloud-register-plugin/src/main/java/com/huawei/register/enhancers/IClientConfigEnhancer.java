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

package com.huawei.register.enhancers;

import com.huawei.sermant.core.agent.definition.EnhanceDefinition;
import com.huawei.sermant.core.agent.definition.MethodInterceptPoint;
import com.huawei.sermant.core.agent.matcher.ClassMatcher;
import com.huawei.sermant.core.agent.matcher.ClassMatchers;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.matcher.ElementMatchers;

/**
 * 针对eureka，consul注册中心获取实例列表拦截
 *
 * @author zhouss
 * @since 2021-12-17
 */
public class IClientConfigEnhancer implements EnhanceDefinition {

    /**
     * 增强类的全限定名
     * 该client注入优先级最高，因此只需拦截该client即可
     */
    private static final String ENHANCE_CLASS = "com.netflix.client.config.IClientConfig";

    /**
     * 拦截类的全限定名
     */
    private static final String INTERCEPT_CLASS = "com.huawei.register.interceptors.IClientConfigInterceptor";

    @Override
    public ClassMatcher enhanceClass() {
        return ClassMatchers.hasSuperTypes(ENHANCE_CLASS);
    }

    @Override
    public MethodInterceptPoint[] getMethodInterceptPoints() {
        return new MethodInterceptPoint[] {
                MethodInterceptPoint.newConstructorInterceptPoint(INTERCEPT_CLASS,
                        ElementMatchers.<MethodDescription>any())
        };
    }
}
