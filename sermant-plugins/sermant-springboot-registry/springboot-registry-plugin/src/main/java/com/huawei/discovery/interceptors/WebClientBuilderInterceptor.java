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

package com.huawei.discovery.interceptors;

import com.huawei.discovery.entity.AbstractRetryExchangeFilterFunction;
import com.huawei.discovery.entity.LowerVersionRetryExchangeFilterFunction;
import com.huawei.discovery.entity.RetryExchangeFilterFunction;

import com.huaweicloud.sermant.core.plugin.agent.entity.ExecuteContext;
import com.huaweicloud.sermant.core.plugin.agent.interceptor.AbstractInterceptor;
import com.huaweicloud.sermant.core.utils.ClassUtils;
import com.huaweicloud.sermant.core.utils.ReflectUtils;

import org.springframework.http.client.reactive.JettyClientHttpConnector;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClient.Builder;

import java.util.List;
import java.util.Optional;

/**
 * webclient拦截点
 *
 * @author provenceee
 * @since 2023-04-25
 */
public class WebClientBuilderInterceptor extends AbstractInterceptor {
    private static final String ABSTRACT_EXCHANGE_FILTER_FUNCTION_CLASS_NAME
            = "com.huawei.discovery.entity.AbstractRetryExchangeFilterFunction";

    private static final String LOWER_VERSION_EXCHANGE_FILTER_FUNCTION_CLASS_NAME
            = "com.huawei.discovery.entity.LowerVersionRetryExchangeFilterFunction";

    private static final String EXCHANGE_FILTER_FUNCTION_CLASS_NAME
            = "com.huawei.discovery.entity.RetryExchangeFilterFunction";

    private static volatile boolean init;

    private Boolean isHigherVersion;

    @Override
    public ExecuteContext before(ExecuteContext context) {
        if (context.getObject() instanceof Builder) {
            Builder builder = (Builder) context.getObject();
            Optional<Object> connector = ReflectUtils.getFieldValue(builder, "connector");
            if (connector.isPresent() && connector.get() instanceof JettyClientHttpConnector) {
                // JettyClientHttpConnector存在bug，不能注入重试过滤器，否则重试时会报
                // IllegalStateException: multiple subscribers not supported
                return context;
            }

            // 初始化
            init();

            // 已经注入重试的不再注入
            Optional<Object> filters = ReflectUtils.getFieldValue(builder, "filters");
            if (filters.isPresent()) {
                List<ExchangeFilterFunction> list = (List<ExchangeFilterFunction>) filters.get();
                for (ExchangeFilterFunction filterFunction : list) {
                    if (filterFunction instanceof AbstractRetryExchangeFilterFunction) {
                        return context;
                    }
                }
            }
            context.skip(getRetryWebClient(builder));
        }
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
                    ClassLoader classLoader = getClass().getClassLoader();
                    ClassUtils.defineClass(ABSTRACT_EXCHANGE_FILTER_FUNCTION_CLASS_NAME, classLoader);
                    ClassUtils.defineClass(LOWER_VERSION_EXCHANGE_FILTER_FUNCTION_CLASS_NAME, classLoader);
                    ClassUtils.defineClass(EXCHANGE_FILTER_FUNCTION_CLASS_NAME, classLoader);
                    init = true;
                }
            }
        }
    }

    private WebClient getRetryWebClient(Builder builder) {
        // clone是为了防止业务应用重复使用一个builder时，会错误地注入RetryExchangeFilterFunction
        Builder retryBuilder = builder.clone();
        if (isHigherVersion == null) {
            try {
                retryBuilder.filter(new RetryExchangeFilterFunction());
                isHigherVersion = true;
            } catch (NoClassDefFoundError error) {
                retryBuilder.filter(new LowerVersionRetryExchangeFilterFunction());
                isHigherVersion = false;
            }
        } else if (isHigherVersion) {
            retryBuilder.filter(new RetryExchangeFilterFunction());
        } else {
            retryBuilder.filter(new LowerVersionRetryExchangeFilterFunction());
        }
        return retryBuilder.build();
    }
}