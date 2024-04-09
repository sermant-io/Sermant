/*
 * Copyright (C) 2022-2024 Sermant Authors. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package com.huaweicloud.sermant.router.spring.interceptor;

import com.huaweicloud.sermant.core.plugin.agent.entity.ExecuteContext;
import com.huaweicloud.sermant.core.plugin.agent.interceptor.AbstractInterceptor;
import com.huaweicloud.sermant.core.utils.ClassUtils;

/**
 * Load some of the necessary classes at startup
 *
 * @author yuzl Yu Zhenlong
 * @since 2022-10-26
 */
public class SpringBootLoadClassInterceptor extends AbstractInterceptor {
    private static final String REQUEST_CALLBACK_WRAPPER =
            "com.huaweicloud.sermant.router.spring.wrapper.RequestCallbackWrapper";

    @Override
    public ExecuteContext before(ExecuteContext context) {
        ClassUtils.defineClass(REQUEST_CALLBACK_WRAPPER, getClass().getClassLoader());
        return context;
    }

    @Override
    public ExecuteContext after(ExecuteContext context) {
        return context;
    }
}
