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

package com.huawei.gray.feign.definition;

import com.huawei.sermant.core.agent.definition.EnhanceDefinition;
import com.huawei.sermant.core.agent.definition.MethodInterceptPoint;
import com.huawei.sermant.core.agent.matcher.ClassMatcher;
import com.huawei.sermant.core.agent.matcher.ClassMatchers;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.matcher.ElementMatchers;

/**
 * 获取服务下游列表增强
 *
 * @author fuziye
 * @since 2021-12-29
 */
public class ServiceInstanceListSupplierDefinition implements EnhanceDefinition {
    private static final String ENHANCE_CLASS_DISCOVERY = "org.springframework.cloud.loadbalancer.core.DiscoveryClientServiceInstanceListSupplier";
    private static final String ENHANCE_CLASS_CACHING = "org.springframework.cloud.loadbalancer.core.CachingServiceInstanceListSupplier";
    private static final String INTERCEPT_CLASS = "com.huawei.gray.feign.interceptor.ServiceInstanceListSupplierInterceptor";
    private static final String ENHANCE_METHOD = "get";

    @Override
    public ClassMatcher enhanceClass() {
        return ClassMatchers.multiClass(ENHANCE_CLASS_DISCOVERY, ENHANCE_CLASS_CACHING);
    }

    @Override
    public MethodInterceptPoint[] getMethodInterceptPoints() {
        return new MethodInterceptPoint[]{
                MethodInterceptPoint.newInstMethodInterceptPoint(INTERCEPT_CLASS,
                        ElementMatchers.<MethodDescription>named(ENHANCE_METHOD))};
    }
}
