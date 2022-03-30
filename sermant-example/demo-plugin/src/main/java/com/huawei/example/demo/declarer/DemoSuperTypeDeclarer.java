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
 * 通过超类方式定位到拦截点的增强定义，本示例将测试构造函数、静态方法和示例方法三种拦截点
 *
 * @author HapThorin
 * @version 1.0.0
 * @since 2021-10-25
 */
public class DemoSuperTypeDeclarer extends AbstractPluginDeclarer {
    @Override
    public ClassMatcher getClassMatcher() {
        return ClassMatcher.isExtendedFrom("com.huawei.example.demo.service.DemoInterface");
    }

    @Override
    public InterceptDeclarer[] getInterceptDeclarers(ClassLoader classLoader) {
        return new InterceptDeclarer[]{
                InterceptDeclarer.build(MethodMatcher.nameEquals("staticFunc"), new DemoStaticInterceptor()),
                InterceptDeclarer.build(MethodMatcher.isConstructor(), new DemoConstInterceptor()),
                InterceptDeclarer.build(MethodMatcher.nameEquals("memberFunc"), new DemoMemberInterceptor())
        };
    }
}
