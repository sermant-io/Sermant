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

package io.sermant.discovery.interceptors;

import io.sermant.core.plugin.agent.entity.ExecuteContext;
import io.sermant.core.plugin.agent.interceptor.AbstractInterceptor;
import io.sermant.core.utils.ClassUtils;
import io.sermant.core.utils.ReflectUtils;
import io.sermant.discovery.entity.AbstractRetryExchangeFilterFunction;
import io.sermant.discovery.entity.LowerVersionRetryExchangeFilterFunction;
import io.sermant.discovery.entity.RetryExchangeFilterFunction;

import org.springframework.http.client.reactive.JettyClientHttpConnector;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClient.Builder;

import java.util.List;
import java.util.Optional;

/**
 * webclient Interception points
 *
 * @author provenceee
 * @since 2023-04-25
 */
public class WebClientBuilderInterceptor extends AbstractInterceptor {
    private static final String ABSTRACT_EXCHANGE_FILTER_FUNCTION_CLASS_NAME
            = "io.sermant.discovery.entity.AbstractRetryExchangeFilterFunction";

    private static final String LOWER_VERSION_EXCHANGE_FILTER_FUNCTION_CLASS_NAME
            = "io.sermant.discovery.entity.LowerVersionRetryExchangeFilterFunction";

    private static final String EXCHANGE_FILTER_FUNCTION_CLASS_NAME
            = "io.sermant.discovery.entity.RetryExchangeFilterFunction";

    private static volatile boolean init;

    private Boolean isHigherVersion;

    @Override
    public ExecuteContext before(ExecuteContext context) {
        if (context.getObject() instanceof Builder) {
            Builder builder = (Builder) context.getObject();
            Optional<Object> connector = ReflectUtils.getFieldValue(builder, "connector");
            if (connector.isPresent() && connector.get() instanceof JettyClientHttpConnector) {
                // There is a bug in the Jetty Client Http Connector, and the retry filter cannot be injected,
                // otherwise it will be reported when it is retried
                // IllegalStateException: multiple subscribers not supported
                return context;
            }

            // Initialize
            init();

            // Injected retries will no longer be injected
            Optional<Object> filters = ReflectUtils.getFieldValue(builder, "filters");
            if (!filters.isPresent()) {
                context.skip(getRetryWebClient(builder));
                return context;
            }
            List<ExchangeFilterFunction> list = (List<ExchangeFilterFunction>) filters.get();
            for (ExchangeFilterFunction filterFunction : list) {
                if (filterFunction instanceof AbstractRetryExchangeFilterFunction) {
                    return context;
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
        // clone is to prevent business applications from incorrectly injecting the RetryExchangeFilter function when
        // reusing a builder
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