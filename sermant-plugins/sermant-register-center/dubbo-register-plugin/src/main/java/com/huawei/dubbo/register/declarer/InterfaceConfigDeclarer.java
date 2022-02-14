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

package com.huawei.dubbo.register.declarer;

import com.huawei.sermant.core.plugin.agent.declarer.InterceptDeclarer;
import com.huawei.sermant.core.plugin.agent.matcher.MethodMatcher;

/**
 * AbstractInterfaceConfig增强类
 *
 * @author provenceee
 * @since 2021/11/24
 */
public class InterfaceConfigDeclarer extends AbstractDeclarer {
    private static final String[] ENHANCE_CLASS = {"org.apache.dubbo.config.AbstractInterfaceConfig",
        "com.alibaba.dubbo.config.AbstractInterfaceConfig"};

    private static final String INTERCEPT_CLASS = "com.huawei.dubbo.register.interceptor.InterfaceConfigInterceptor";

    // 增强loadRegistriesFromBackwardConfig方法是为了兼容2.7.0-2.7.4.1，其它版本主要是增强setRegistries方法
    private static final String[] METHOD_NAME = {"setRegistries", "loadRegistriesFromBackwardConfig"};

    public InterfaceConfigDeclarer() {
        super(ENHANCE_CLASS);
    }

    @Override
    public InterceptDeclarer[] getInterceptDeclarers(ClassLoader classLoader) {
        return new InterceptDeclarer[]{
            InterceptDeclarer.build(MethodMatcher.nameContains(METHOD_NAME), INTERCEPT_CLASS)
        };
    }
}
