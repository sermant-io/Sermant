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

import com.huawei.example.demo.common.DemoBeanPropertyApi;
import com.huawei.example.demo.common.DemoInterfaceApi;
import com.huawei.example.demo.common.DemoLogger;
import com.huawei.sermant.core.plugin.agent.entity.ExecuteContext;
import com.huawei.sermant.core.plugin.agent.interceptor.AbstractInterceptor;

/**
 * 用于测试对被增强类的接口实现
 *
 * @author HapThorin
 * @version 1.0.0
 * @since 2022-01-22
 */
public class DemoInterfaceInterceptor extends AbstractInterceptor {
    @Override
    public ExecuteContext before(ExecuteContext context) throws Exception {
        final Object object = context.getObject();
        if (object instanceof DemoInterfaceApi) {
            ((DemoInterfaceApi) object).foo();
        }
        if (object instanceof DemoBeanPropertyApi) {
            final DemoBeanPropertyApi demoBeanPropertyApi = (DemoBeanPropertyApi) object;
            demoBeanPropertyApi.setFoo("foo field string");
            demoBeanPropertyApi.setBar(1);
        }
        return context;
    }

    @Override
    public ExecuteContext after(ExecuteContext context) throws Exception {
        final Object object = context.getObject();
        if (object instanceof DemoBeanPropertyApi) {
            final DemoBeanPropertyApi demoBeanPropertyApi = (DemoBeanPropertyApi) object;
            DemoLogger.println("[DemoInterfaceInterceptor]-foo: " + demoBeanPropertyApi.getFoo());
            DemoLogger.println("[DemoInterfaceInterceptor]-bar: " + demoBeanPropertyApi.getBar());
        }
        return context;
    }
}
