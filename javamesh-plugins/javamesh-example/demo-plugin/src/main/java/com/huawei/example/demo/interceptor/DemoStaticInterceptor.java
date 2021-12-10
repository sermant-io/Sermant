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

package com.huawei.example.demo.interceptor;

import java.lang.reflect.Method;

import com.huawei.javamesh.core.agent.common.BeforeResult;
import com.huawei.javamesh.core.agent.interceptor.StaticMethodInterceptor;
import com.huawei.example.demo.common.DemoLogger;

/**
 * 静态函数的拦截器示例，本示例将展示如何对静态函数进行增强
 *
 * @author HapThorin
 * @version 1.0.0
 * @since 2021/10/25
 */
public class DemoStaticInterceptor implements StaticMethodInterceptor {
    @Override
    public void before(Class<?> clazz, Method method, Object[] arguments, BeforeResult beforeResult) throws Exception {
        DemoLogger.println(clazz.getSimpleName() + ": [DemoStaticInterceptor]-before");
    }

    @Override
    public Object after(Class<?> clazz, Method method, Object[] arguments, Object result) throws Exception {
        DemoLogger.println(clazz.getSimpleName() + ": [DemoStaticInterceptor]-after");
        return result;
    }

    @Override
    public void onThrow(Class<?> clazz, Method method, Object[] arguments, Throwable t) {
        DemoLogger.println(clazz.getSimpleName() + ": [DemoStaticInterceptor]-onThrow");
    }
}
