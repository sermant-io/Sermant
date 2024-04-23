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

package io.sermant.router.spring.interceptor;

import io.sermant.core.plugin.agent.entity.ExecuteContext;
import io.sermant.core.plugin.agent.interceptor.AbstractInterceptor;
import io.sermant.core.plugin.config.PluginConfigManager;
import io.sermant.router.common.config.TransmitConfig;
import io.sermant.router.common.request.RequestData;
import io.sermant.router.common.request.RequestTag;
import io.sermant.router.common.utils.ThreadLocalUtils;

import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.server.ServerWebExchange;

/**
 * spring cloud gateway ReactiveLoadBalancerClientFilter enhancement the class to get the request data
 *
 * @author provenceee
 * @since 2024-01-16
 */
public class ReactiveLoadBalancerClientFilterInterceptor extends AbstractInterceptor {
    private final TransmitConfig config;

    /**
     * Constructor
     */
    public ReactiveLoadBalancerClientFilterInterceptor() {
        this.config = PluginConfigManager.getPluginConfig(TransmitConfig.class);
    }

    @Override
    public ExecuteContext before(ExecuteContext context) {
        if (context.getArguments()[0] instanceof ServerWebExchange) {
            ServerHttpRequest request = getExchangeAfterPutHeaders(context).getRequest();
            String path = request.getURI().getPath();
            ThreadLocalUtils.setRequestData(new RequestData(request.getHeaders(), path, request.getMethod().name()));
        }
        return context;
    }

    @Override
    public ExecuteContext after(ExecuteContext context) {
        // ReactiveLoadBalancerCentFilter is a responsive programming method
        // that cannot be deleted in the after method when thread pool pass through is enabled.
        // Otherwise, thread variables cannot be passed through to load balancing threads
        // and will be deleted in HttpServerHandleInterceptor and ReactiveTypeHandleInterceptor
        if (!config.isEnabledThreadPool()) {
            ThreadLocalUtils.removeRequestTag();
            ThreadLocalUtils.removeRequestData();
        }
        return context;
    }

    @Override
    public ExecuteContext onThrow(ExecuteContext context) {
        ThreadLocalUtils.removeRequestTag();
        ThreadLocalUtils.removeRequestData();
        return context;
    }

    private ServerWebExchange getExchangeAfterPutHeaders(ExecuteContext context) {
        ServerWebExchange exchange = (ServerWebExchange) context.getArguments()[0];
        RequestTag requestTag = ThreadLocalUtils.getRequestTag();
        if (requestTag == null) {
            return exchange;
        }
        HttpHeaders httpHeaders = HttpHeaders.writableHttpHeaders(exchange.getRequest().getHeaders());
        requestTag.getTag().forEach(httpHeaders::putIfAbsent);
        return exchange;
    }
}