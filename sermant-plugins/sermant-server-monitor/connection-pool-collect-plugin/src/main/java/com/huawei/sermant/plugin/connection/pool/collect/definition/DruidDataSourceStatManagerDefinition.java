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

package com.huawei.sermant.plugin.connection.pool.collect.definition;

import com.huawei.sermant.core.agent.definition.EnhanceDefinition;
import com.huawei.sermant.core.agent.definition.MethodInterceptPoint;
import com.huawei.sermant.core.agent.matcher.ClassMatcher;
import com.huawei.sermant.core.agent.matcher.ClassMatchers;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.matcher.ElementMatchers;

/**
 * Druid Monitor 增强定义
 */
public class DruidDataSourceStatManagerDefinition implements EnhanceDefinition {

    private static final String ENHANCE_CLASS = "com.alibaba.druid.stat.DruidDataSourceStatManager";
    private static final String ADD_DATA_SOURCE_INTERCEPTOR =
        "com.huawei.sermant.sample.connection.pool.collect.interceptor.AddDataSourceInterceptor";
    private static final String REMOVE_DATA_SOURCE_INTERCEPTOR =
        "com.huawei.sermant.sample.connection.pool.collect.interceptor.RemoveDataSourceInterceptor";

    @Override
    public ClassMatcher enhanceClass() {
        return ClassMatchers.named(ENHANCE_CLASS);
    }

    @Override
    public MethodInterceptPoint[] getMethodInterceptPoints() {
        return new MethodInterceptPoint[]{
            MethodInterceptPoint.newStaticMethodInterceptPoint(ADD_DATA_SOURCE_INTERCEPTOR,
                ElementMatchers.<MethodDescription>named("addDataSource")),
            MethodInterceptPoint.newStaticMethodInterceptPoint(REMOVE_DATA_SOURCE_INTERCEPTOR,
                ElementMatchers.<MethodDescription>named("removeDataSource"))
        };
    }
}
