/*
 * Copyright (C) 2023-2023 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huaweicloud.sermant.router.spring.interceptor;

import com.huaweicloud.sermant.core.plugin.agent.entity.ExecuteContext;
import com.huaweicloud.sermant.core.plugin.agent.interceptor.AbstractInterceptor;
import com.huaweicloud.sermant.router.common.utils.ThreadLocalUtils;

/**
 * 拦截ReactiveTypeHandler，同时引入spring-boot-starter-web、spring-boot-starter-webflux进行响应式编程时，需要在后置方法移除线程变量
 * <p>spring cloud Finchley.RELEASE+
 *
 * @author provenceee
 * @since 2023-06-09
 */
public class ReactiveTypeHandlerInterceptor extends AbstractInterceptor {
    @Override
    public ExecuteContext before(ExecuteContext context) {
        return context;
    }

    @Override
    public ExecuteContext after(ExecuteContext context) {
        ThreadLocalUtils.removeRequestData();
        ThreadLocalUtils.removeRequestHeader();
        return context;
    }

    @Override
    public ExecuteContext onThrow(ExecuteContext context) {
        ThreadLocalUtils.removeRequestData();
        ThreadLocalUtils.removeRequestHeader();
        return context;
    }
}