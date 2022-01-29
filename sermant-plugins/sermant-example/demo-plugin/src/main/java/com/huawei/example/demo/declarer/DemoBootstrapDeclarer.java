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

package com.huawei.example.demo.declarer;

import com.huawei.example.demo.interceptor.DemoConstInterceptor;
import com.huawei.example.demo.interceptor.DemoMemberInterceptor;
import com.huawei.example.demo.interceptor.DemoStaticInterceptor;
import com.huawei.sermant.core.plugin.agent.declarer.AbstractPluginDeclarer;
import com.huawei.sermant.core.plugin.agent.declarer.InterceptDeclarer;
import com.huawei.sermant.core.plugin.agent.matcher.ClassMatcher;
import com.huawei.sermant.core.plugin.agent.matcher.MethodMatcher;

/**
 * 以Thread为例测试启动类加载器加载的类的增强情况
 *
 * @author HapThorin
 * @version 1.0.0
 * @since 2021-10-27
 */
public class DemoBootstrapDeclarer extends AbstractPluginDeclarer {
    @Override
    public ClassMatcher getClassMatcher() {
        return ClassMatcher.nameEquals("java.lang.Thread");
    }

    @Override
    public InterceptDeclarer[] getInterceptDeclarers(ClassLoader classLoader) {
        return new InterceptDeclarer[]{
                InterceptDeclarer.build(MethodMatcher.nameEquals("getAllStackTraces"),
                        new DemoStaticInterceptor()), // 测试静态方法
                InterceptDeclarer.build(MethodMatcher.isConstructor().and(MethodMatcher.paramCountEquals(0)),
                        new DemoConstInterceptor()), // 测试无参构造函数
                InterceptDeclarer.build(MethodMatcher.nameEquals("setName"),
                        new DemoMemberInterceptor()), // 测试实例方法
        };
    }
}
