/*
 * Copyright (C) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved
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

package com.huawei.flowrecord.plugins.boot;

import com.huawei.javamesh.core.agent.definition.EnhanceDefinition;
import com.huawei.javamesh.core.agent.definition.MethodInterceptPoint;
import com.huawei.javamesh.core.agent.matcher.ClassMatcher;
import com.huawei.javamesh.core.agent.matcher.ClassMatchers;
import net.bytebuddy.description.method.MethodDescription;

import net.bytebuddy.matcher.ElementMatchers;

/**
 * 拦截注解 org.springframework.boot.autoconfigure.SpringBootApplication的类,也可以拦截应用中启动后立即执行仅一次的类
 * 1、启动线程定时向kafka推送数据
 * 2、开启zookeeper监听器
 *
 */
public class BootInstrumentation implements EnhanceDefinition {
    /**
     * 拦截点位于springbootapplication，仅触发一次
     */
    public static final String ENHANCE_ANNOTATION = "org.springframework.boot.autoconfigure.SpringBootApplication";
    private static final String INTERCEPT_CLASS = "com.huawei.flowrecord.plugins.boot.BootInterceptor";

    @Override
    public ClassMatcher enhanceClass() {
        return ClassMatchers.annotationWith(ENHANCE_ANNOTATION);
    }

    @Override
    public MethodInterceptPoint[] getMethodInterceptPoints() {
        return new MethodInterceptPoint[]{
                MethodInterceptPoint.newStaticMethodInterceptPoint(INTERCEPT_CLASS, ElementMatchers.<MethodDescription>named("main"))
        };
    }
}