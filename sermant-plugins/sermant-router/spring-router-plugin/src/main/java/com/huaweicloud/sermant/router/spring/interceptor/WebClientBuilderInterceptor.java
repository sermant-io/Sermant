/*
 * Copyright (C) 2024-2024 Huawei Technologies Co., Ltd. All rights reserved.
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
import com.huaweicloud.sermant.core.utils.ClassUtils;
import com.huaweicloud.sermant.core.utils.ReflectUtils;

import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient.Builder;

import java.util.List;
import java.util.Optional;

/**
 * 注入请求过滤器
 *
 * @author provenceee
 * @since 2024-01-16
 */
public class WebClientBuilderInterceptor extends AbstractInterceptor {
    private static final String EXCHANGE_FILTER_FUNCTION_CLASS_NAME
            = "com.huaweicloud.sermant.router.spring.interceptor.RouterExchangeFilterFunction";

    private static volatile boolean init;

    @Override
    public ExecuteContext before(ExecuteContext context) {
        if (!(context.getObject() instanceof Builder)) {
            return context;
        }
        init();
        Builder builder = (Builder) context.getObject();
        Optional<Object> filters = ReflectUtils.getFieldValue(builder, "filters");
        if (filters.isPresent()) {
            List<ExchangeFilterFunction> list = (List<ExchangeFilterFunction>) filters.get();
            for (ExchangeFilterFunction filterFunction : list) {
                if (filterFunction instanceof RouterExchangeFilterFunction) {
                    // 已经注入重试的不再注入
                    return context;
                }
            }

            // 存在过滤器时，注入到第一个
            list.add(0, new RouterExchangeFilterFunction());
            return context;
        }
        builder.filter(new RouterExchangeFilterFunction());
        return context;
    }

    @Override
    public ExecuteContext after(ExecuteContext context) {
        return context;
    }

    private void init() {
        if (!init) {
            synchronized (WebClientBuilderInterceptor.class) {
                if (!init) {
                    ClassUtils.defineClass(EXCHANGE_FILTER_FUNCTION_CLASS_NAME, getClass().getClassLoader());
                    init = true;
                }
            }
        }
    }
}