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

package com.huawei.discovery.declarers;

import com.huawei.discovery.interceptors.FeignInvokeInterceptor;

import com.huaweicloud.sermant.core.plugin.agent.declarer.AbstractPluginDeclarer;
import com.huaweicloud.sermant.core.plugin.agent.declarer.InterceptDeclarer;
import com.huaweicloud.sermant.core.plugin.agent.matcher.ClassMatcher;
import com.huaweicloud.sermant.core.plugin.agent.matcher.MethodMatcher;

/**
 * 针对feign请求方式，从注册中心获取实例列表拦截
 *
 * @author chengyouling
 * @since 2022-09-27
 */
public class FeignInvokeDeclarer extends AbstractPluginDeclarer {

    private static final String INTERCEPT_CLASS = FeignInvokeInterceptor.class.getCanonicalName();

    private static final String METHOD_NAME = "execute";

    @Override
    public ClassMatcher getClassMatcher() {
        return ClassMatcher.isExtendedFrom("feign.Client");
    }

    @Override
    public InterceptDeclarer[] getInterceptDeclarers(ClassLoader classLoader) {
        return new InterceptDeclarer[] {
                InterceptDeclarer.build(MethodMatcher.nameEquals(METHOD_NAME), INTERCEPT_CLASS)
        };
    }
}
