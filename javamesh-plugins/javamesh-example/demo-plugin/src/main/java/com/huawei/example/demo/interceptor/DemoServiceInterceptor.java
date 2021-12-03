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

package com.huawei.example.demo.interceptor;

import java.lang.reflect.Method;

import com.huawei.javamesh.core.agent.common.BeforeResult;
import com.huawei.javamesh.core.agent.interceptor.StaticMethodInterceptor;
import com.huawei.javamesh.core.plugin.service.PluginServiceManager;
import com.huawei.example.demo.common.DemoLogger;
import com.huawei.example.demo.service.DemoComplexService;
import com.huawei.example.demo.service.DemoSimpleService;

/**
 * 插件服务的拦截器示例，在本示例中，将展示如何在拦截器中使用插件服务
 *
 * @author HapThorin
 * @version 1.0.0
 * @since 2021/10/25
 */
public class DemoServiceInterceptor implements StaticMethodInterceptor {
    private DemoSimpleService simpleService;
    private DemoComplexService complexService;

    @Override
    public void before(Class<?> clazz, Method method, Object[] arguments, BeforeResult beforeResult) throws Exception {
        DemoLogger.println(clazz.getSimpleName() + ": [DemoServiceInterceptor]-before");
        simpleService = PluginServiceManager.getPluginService(DemoSimpleService.class);
        complexService = PluginServiceManager.getPluginService(DemoComplexService.class);
    }

    @Override
    public Object after(Class<?> clazz, Method method, Object[] arguments, Object result) throws Exception {
        DemoLogger.println(clazz.getSimpleName() + ": [DemoServiceInterceptor]-after");
        simpleService.activeFunc();
        complexService.activeFunc();
        return result;
    }

    @Override
    public void onThrow(Class<?> clazz, Method method, Object[] arguments, Throwable t) {
        DemoLogger.println(clazz.getSimpleName() + ": [DemoServiceInterceptor]-onThrow");
    }
}
