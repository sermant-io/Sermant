/*
 * Copyright (C) 2021-2024 Sermant Authors. All rights reserved.
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

package com.huawei.monitor.declarer;

import com.huaweicloud.sermant.core.plugin.agent.declarer.AbstractPluginDeclarer;
import com.huaweicloud.sermant.core.plugin.agent.declarer.InterceptDeclarer;
import com.huaweicloud.sermant.core.plugin.agent.matcher.ClassMatcher;
import com.huaweicloud.sermant.core.plugin.agent.matcher.MethodMatcher;

/**
 * instance enhancement base class
 *
 * @author provenceee
 * @since 2021-11-24
 */
public abstract class AbstractDeclarer extends AbstractPluginDeclarer {
    private final String[] enhanceClass;

    private final String interceptClass;

    private final String methodName;

    /**
     * constructor
     *
     * @param enhanceClass enhancement class
     * @param interceptClass intercept class
     * @param methodName enhancement method
     */
    public AbstractDeclarer(String[] enhanceClass, String interceptClass, String methodName) {
        this.enhanceClass = enhanceClass;
        this.interceptClass = interceptClass;
        this.methodName = methodName;
    }

    @Override
    public ClassMatcher getClassMatcher() {
        return ClassMatcher.nameContains(enhanceClass);
    }

    @Override
    public InterceptDeclarer[] getInterceptDeclarers(ClassLoader classLoader) {
        return new InterceptDeclarer[]{
            InterceptDeclarer.build(getMethodMatcher(), interceptClass)
        };
    }

    /**
     * getMethodMatcher
     *
     * @return methodMatcher
     */
    public MethodMatcher getMethodMatcher() {
        return MethodMatcher.nameEquals(methodName);
    }
}