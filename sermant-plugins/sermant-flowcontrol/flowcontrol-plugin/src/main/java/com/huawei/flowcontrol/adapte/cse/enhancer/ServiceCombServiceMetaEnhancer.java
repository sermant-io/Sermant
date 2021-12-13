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

package com.huawei.flowcontrol.adapte.cse.enhancer;

import com.huawei.sermant.core.agent.definition.EnhanceDefinition;
import com.huawei.sermant.core.agent.definition.MethodInterceptPoint;
import com.huawei.sermant.core.agent.matcher.ClassMatcher;
import com.huawei.sermant.core.agent.matcher.ClassMatchers;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.matcher.ElementMatchers;

/**
 * 拦截servicecomb的服务名与版本
 *
 * @author zhouss
 * @since 2021-11-16
 */
public class ServiceCombServiceMetaEnhancer implements EnhanceDefinition {
    /**
     * 拦截器
     */
    private static final String INTERCEPTOR_CLASS = "com.huawei.flowcontrol.adapte.cse.interceptors.MetricServiceMetaInterceptor";

    @Override
    public ClassMatcher enhanceClass() {
        return ClassMatchers.hasSuperTypes("org.apache.servicecomb.governance.MicroserviceMeta");
    }

    @Override
    public MethodInterceptPoint[] getMethodInterceptPoints() {
        return new MethodInterceptPoint[] {
                MethodInterceptPoint.newInstMethodInterceptPoint(INTERCEPTOR_CLASS,
                        ElementMatchers.<MethodDescription>named("getName")
                                .or(ElementMatchers.<MethodDescription>named("getVersion")))
        };
    }
}
