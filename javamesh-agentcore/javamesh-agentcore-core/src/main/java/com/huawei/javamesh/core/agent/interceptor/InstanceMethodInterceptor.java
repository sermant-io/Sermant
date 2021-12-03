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

package com.huawei.javamesh.core.agent.interceptor;

import com.huawei.javamesh.core.agent.common.BeforeResult;

import java.lang.reflect.Method;

/**
 * 实例方法拦截器接口
 */
public interface InstanceMethodInterceptor extends Interceptor {

    /**
     * 前置方法
     *
     * @param obj          增强实例
     * @param method       原方法
     * @param arguments    原方法参数
     * @param beforeResult 前置结果
     * @throws Exception 前置方法异常
     */
    void before(Object obj, Method method, Object[] arguments, BeforeResult beforeResult) throws Exception;

    /**
     * 后置方法
     *
     * @param obj       增强实例
     * @param method    原方法
     * @param arguments 原方法参数
     * @param result    原方法结果
     * @throws Exception 后置方法异常
     */
    Object after(Object obj, Method method, Object[] arguments, Object result) throws Exception;

    /**
     * 异常处理方法
     *
     * @param obj       增强实例
     * @param method    原方法
     * @param arguments 原方法参数
     * @param t         原方法异常
     */
    void onThrow(Object obj, Method method, Object[] arguments, Throwable t);
}
