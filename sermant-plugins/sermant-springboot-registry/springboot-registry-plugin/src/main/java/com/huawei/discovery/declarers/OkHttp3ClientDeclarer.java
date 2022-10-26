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

import com.huawei.discovery.interceptors.OkHttp3ClientInterceptor;

import com.huaweicloud.sermant.core.plugin.agent.declarer.AbstractPluginDeclarer;
import com.huaweicloud.sermant.core.plugin.agent.declarer.InterceptDeclarer;
import com.huaweicloud.sermant.core.plugin.agent.matcher.ClassMatcher;
import com.huaweicloud.sermant.core.plugin.agent.matcher.MethodMatcher;

/**
 * 针对okhttp请求方式，从注册中心获取实例列表拦截
 *
 * @author chengyouling
 * @since 2022-09-17
 */
public class OkHttp3ClientDeclarer extends AbstractPluginDeclarer {
    /**
     * 增强类的全限定名 okhttp请求
     */
    private static final String[] ENHANCE_CLASSES = {
        "okhttp3.RealCall",
        "okhttp3.internal.connection.RealCall"
    };

    /**
     * 拦截类的全限定名
     */
    private static final String INTERCEPT_CLASS = OkHttp3ClientInterceptor.class.getCanonicalName();

    @Override
    public ClassMatcher getClassMatcher() {
        return ClassMatcher.nameContains(ENHANCE_CLASSES);
    }

    @Override
    public InterceptDeclarer[] getInterceptDeclarers(ClassLoader classLoader) {
        return new InterceptDeclarer[] {
                InterceptDeclarer.build(MethodMatcher.nameContains("execute", "getResponseWithInterceptorChain"),
                        INTERCEPT_CLASS)
        };
    }
}
