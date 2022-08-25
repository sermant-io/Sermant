/*
 * Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.huawei.dynamic.config.declarers;

import com.huawei.dynamic.config.interceptors.MutableSourceInterceptor;

import com.huaweicloud.sermant.core.plugin.agent.declarer.AbstractPluginDeclarer;
import com.huaweicloud.sermant.core.plugin.agent.declarer.InterceptDeclarer;
import com.huaweicloud.sermant.core.plugin.agent.matcher.ClassMatcher;
import com.huaweicloud.sermant.core.plugin.agent.matcher.MethodMatcher;

/**
 * 针对addFirst方法拦截, 当用户在配置中心下发配置, 动态关闭原生配置中心, 此拦截点开始拦截添加原生配置中心配置源, 阻止配置生效
 * 生效见{@link com.huawei.dynamic.config.source.OriginConfigCenterDisableListener}添加禁止配置源
 *
 * @author zhouss
 * @since 2022-04-08
 */
public class MutableSourceDeclarer extends AbstractPluginDeclarer {
    private static final String ENHANCE_CLASS =
            "org.springframework.core.env.MutablePropertySources";

    private static final String INTERCEPTOR_CLASS = MutableSourceInterceptor.class.getCanonicalName();

    @Override
    public ClassMatcher getClassMatcher() {
        return ClassMatcher.nameEquals(ENHANCE_CLASS);
    }

    @Override
    public InterceptDeclarer[] getInterceptDeclarers(ClassLoader classLoader) {
        return new InterceptDeclarer[]{
                InterceptDeclarer.build(MethodMatcher.nameEquals("addFirst").and(MethodMatcher.paramCountEquals(1)),
                        INTERCEPTOR_CLASS)
        };
    }
}
