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

import com.huawei.example.demo.common.DemoBeanPropertyApi;
import com.huawei.example.demo.common.DemoInterfaceApi;
import com.huawei.example.demo.common.DemoInterfaceImpl;
import com.huawei.example.demo.interceptor.DemoConfigInterceptor;
import com.huawei.example.demo.interceptor.DemoConstInterceptor;
import com.huawei.example.demo.interceptor.DemoFieldCheckInterceptor;
import com.huawei.example.demo.interceptor.DemoFieldSetInterceptor;
import com.huawei.example.demo.interceptor.DemoInterfaceInterceptor;
import com.huawei.example.demo.interceptor.DemoMemberInterceptor;
import com.huawei.example.demo.interceptor.DemoServiceInterceptor;
import com.huawei.example.demo.interceptor.DemoStaticInterceptor;
import com.huawei.sermant.core.plugin.agent.declarer.AbstractPluginDeclarer;
import com.huawei.sermant.core.plugin.agent.declarer.InterceptDeclarer;
import com.huawei.sermant.core.plugin.agent.declarer.SuperTypeDeclarer;
import com.huawei.sermant.core.plugin.agent.matcher.ClassMatcher;
import com.huawei.sermant.core.plugin.agent.matcher.MethodMatcher;

/**
 * 通过名称定位到拦截点的增强定义，本示例处理测试构造函数、静态方法和示例方法三种拦截点外，还会测试日志功能和统一配置
 * <p>本示例直接使用名称完全匹配的方式定位，其他名称相关的定位方式有：
 * <pre>
 *     1. {@link ClassMatcher#nameEquals(String)}完全匹配
 *     2. {@link ClassMatcher#nameContains} 多重匹配
 *     3. {@link ClassMatcher#namePrefixedWith(String)} 前缀匹配
 *     4. {@link ClassMatcher#nameSuffixedWith(String)} 后缀匹配
 *     5. {@link ClassMatcher#nameInfixedWith(String)} 内容包含
 *     6. {@link ClassMatcher#nameMatches(String)} 正则匹配
 * </pre>
 *
 * @author HapThorin
 * @version 1.0.0
 * @since 2021-10-25
 */
public class DemoNameDeclarer extends AbstractPluginDeclarer {
    @Override
    public ClassMatcher getClassMatcher() {
        return ClassMatcher.nameEquals("com.huawei.example.demo.service.DemoNameService");
    }

    @Override
    public InterceptDeclarer[] getInterceptDeclarers(ClassLoader classLoader) {
        return new InterceptDeclarer[]{
                InterceptDeclarer.build(MethodMatcher.nameEquals("staticFunc"), new DemoStaticInterceptor()),
                InterceptDeclarer.build(MethodMatcher.isConstructor(), new DemoConstInterceptor()),
                InterceptDeclarer.build(MethodMatcher.nameEquals("memberFunc"), new DemoMemberInterceptor()),
                InterceptDeclarer.build(MethodMatcher.nameEquals("serviceFunc"), new DemoServiceInterceptor()),
                InterceptDeclarer.build(MethodMatcher.nameEquals("configFunc"), new DemoConfigInterceptor()),
                InterceptDeclarer.build(MethodMatcher.nameEquals("fieldFunc"),
                        new DemoFieldSetInterceptor(), new DemoFieldCheckInterceptor()),
                InterceptDeclarer.build(MethodMatcher.isMemberMethod().and(MethodMatcher.nameEquals("interfaceFunc")),
                        new DemoInterfaceInterceptor())
        };
    }

    @Override
    public SuperTypeDeclarer[] getSuperTypeDeclarers() {
        return new SuperTypeDeclarer[]{
                SuperTypeDeclarer.ForImplInstance.build(DemoInterfaceApi.class, new DemoInterfaceImpl()),
                SuperTypeDeclarer.ForBeanProperty.build(DemoBeanPropertyApi.class)
        };
    }
}
