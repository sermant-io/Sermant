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

package com.huawei.gray.dubbo.definition;

import com.huawei.sermant.core.agent.definition.EnhanceDefinition;
import com.huawei.sermant.core.agent.definition.MethodInterceptPoint;
import com.huawei.sermant.core.agent.matcher.ClassMatcher;
import com.huawei.sermant.core.agent.matcher.ClassMatchers;

import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.matcher.ElementMatchers;

/**
 * 实例增强基类
 *
 * @author provenceee
 * @since 2021/11/24
 */
public abstract class AbstractInstDefinition implements EnhanceDefinition {
    private final String enhanceClass;

    private final String interceptClass;

    private final String methodName;

    /**
     * 构造方法
     *
     * @param enhanceClass 增加类
     * @param interceptClass 拦截类
     * @param methodName 拦截方法
     */
    public AbstractInstDefinition(String enhanceClass, String interceptClass, String methodName) {
        this.enhanceClass = enhanceClass;
        this.interceptClass = interceptClass;
        this.methodName = methodName;
    }

    @Override
    public ClassMatcher enhanceClass() {
        return ClassMatchers.named(enhanceClass);
    }

    @Override
    public MethodInterceptPoint[] getMethodInterceptPoints() {
        return new MethodInterceptPoint[]{
            MethodInterceptPoint.newInstMethodInterceptPoint(interceptClass,
                ElementMatchers.<MethodDescription>named(methodName))};
    }
}
