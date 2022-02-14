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

package com.huawei.dubbo.register.declarer;

import com.huawei.sermant.core.plugin.agent.declarer.AbstractPluginDeclarer;
import com.huawei.sermant.core.plugin.agent.matcher.ClassMatcher;
import com.huawei.sermant.core.plugin.agent.matcher.MethodMatcher;

/**
 * 实例增强基类
 *
 * @author provenceee
 * @since 2022/1/18
 */
public abstract class AbstractDeclarer extends AbstractPluginDeclarer {
    private final String[] enhanceClass;

    /**
     * 构造方法
     *
     * @param enhanceClass 增加类
     */
    public AbstractDeclarer(String[] enhanceClass) {
        this.enhanceClass = enhanceClass;
    }

    @Override
    public ClassMatcher getClassMatcher() {
        return ClassMatcher.nameContains(enhanceClass);
    }

    /**
     * 获取静态方法匹配器
     *
     * @param methodName 方法名
     * @return 静态方法匹配器
     */
    protected MethodMatcher getStaticMethod(String methodName) {
        return MethodMatcher.nameEquals(methodName).and(MethodMatcher.isStaticMethod());
    }
}