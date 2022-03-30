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

import com.huawei.example.demo.common.DemoLogger;
import com.huawei.example.demo.service.DemoComplexService;
import com.huawei.example.demo.service.DemoSimpleService;
import com.huawei.sermant.core.plugin.agent.entity.ExecuteContext;
import com.huawei.sermant.core.plugin.agent.interceptor.AbstractInterceptor;
import com.huawei.sermant.core.plugin.service.PluginServiceManager;

/**
 * 插件服务的拦截器示例，在本示例中，将展示如何在拦截器中使用插件服务
 *
 * @author HapThorin
 * @version 1.0.0
 * @since 2021-10-25
 */
public class DemoServiceInterceptor extends AbstractInterceptor {
    private DemoSimpleService simpleService;
    private DemoComplexService complexService;

    @Override
    public ExecuteContext before(ExecuteContext context) throws Exception {
        DemoLogger.println(context.getRawCls().getSimpleName() + ": [DemoServiceInterceptor]-before");
        simpleService = PluginServiceManager.getPluginService(DemoSimpleService.class);
        complexService = PluginServiceManager.getPluginService(DemoComplexService.class);
        return context;
    }

    @Override
    public ExecuteContext after(ExecuteContext context) throws Exception {
        DemoLogger.println(context.getRawCls().getSimpleName() + ": [DemoServiceInterceptor]-after");
        simpleService.activeFunc();
        complexService.activeFunc();
        return context;
    }
}
