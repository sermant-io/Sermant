/*
 * Copyright (C) 2021-2022 Huawei Technologies Co., Ltd. All rights reserved.
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
import com.huawei.sermant.core.plugin.agent.entity.ExecuteContext;
import com.huawei.sermant.core.plugin.agent.interceptor.AbstractInterceptor;

/**
 * 用于测试局部变量、成员变量和全局变量跨拦截器读取
 *
 * @author HapThorin
 * @version 1.0.0
 * @since 2022-01-22
 */
public class DemoFieldCheckInterceptor extends AbstractInterceptor {
    @Override
    public ExecuteContext before(ExecuteContext context) throws Exception {
        return context;
    }

    @Override
    public ExecuteContext after(ExecuteContext context) throws Exception {
        DemoLogger.println("[DemoFieldCheckInterceptor]-testLocal: " + context.getLocalFieldValue("testLocal"));
        DemoLogger.println("[DemoFieldCheckInterceptor]-testMember: " + context.getMemberFieldValue("testMember"));
        DemoLogger.println("[DemoFieldCheckInterceptor]-testStatic: " + context.getStaticFieldValue("testStatic"));
        return context;
    }
}
