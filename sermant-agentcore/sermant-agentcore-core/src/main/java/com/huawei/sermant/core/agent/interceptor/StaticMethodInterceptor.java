/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 * Based on org/apache/skywalking/apm/agent/core/plugin/interceptor/enhance/StaticMethodsAroundInterceptor.java
 * from the Apache Skywalking project.
 */

package com.huawei.sermant.core.agent.interceptor;

import com.huawei.sermant.core.agent.common.BeforeResult;

import java.lang.reflect.Method;

/**
 * 静态方法拦截器接口
 */
@Deprecated
public interface StaticMethodInterceptor extends Interceptor {

    /**
     * 前置方法
     *
     * @param clazz        增强实例class对象
     * @param method       原方法
     * @param arguments    原方法参数
     * @param beforeResult 前置结果
     * @throws Exception 前置方法异常
     */
    void before(Class<?> clazz, Method method, Object[] arguments, BeforeResult beforeResult) throws Exception;

    /**
     * 后置方法
     *
     * @param clazz     增强实例class对象
     * @param method    原方法
     * @param arguments 原方法参数
     * @param result    原方法结果
     * @throws Exception 后置方法异常
     */
    Object after(Class<?> clazz, Method method, Object[] arguments, Object result) throws Exception;

    /**
     * 异常处理方法
     *
     * @param clazz     增强实例class对象
     * @param method    原方法
     * @param arguments 原方法参数
     * @param t         原方法异常
     */
    void onThrow(Class<?> clazz, Method method, Object[] arguments, Throwable t);
}
