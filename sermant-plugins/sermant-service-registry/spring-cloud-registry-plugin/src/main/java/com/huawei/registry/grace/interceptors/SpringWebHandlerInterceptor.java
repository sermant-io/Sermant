/*
 * Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.huawei.registry.grace.interceptors;

import com.huawei.registry.inject.grace.SpringRequestInterceptor;

import com.huaweicloud.sermant.core.plugin.agent.entity.ExecuteContext;

import org.springframework.web.servlet.HandlerExecutionChain;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 注入请求拦截器
 *
 * @author zhouss
 * @since 2022-05-23
 */
public class SpringWebHandlerInterceptor extends GraceSwitchInterceptor {
    private HandlerInterceptor handlerInterceptor;

    @Override
    protected ExecuteContext doBefore(ExecuteContext context) {
        final Object object = context.getObject();
        if (object instanceof HandlerExecutionChain) {
            HandlerExecutionChain chain = (HandlerExecutionChain) object;
            chain.addInterceptor(getInterceptor());
        }
        return context;
    }

    private HandlerInterceptor getInterceptor() {
        if (handlerInterceptor == null) {
            handlerInterceptor = new SpringRequestInterceptor();
        }
        return handlerInterceptor;
    }
}
