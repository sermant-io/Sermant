/*
 * Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
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
import com.huaweicloud.sermant.core.utils.ReflectUtils;
import com.huaweicloud.sermant.router.common.request.RequestData;
import com.huaweicloud.sermant.router.common.request.RequestTag;
import com.huaweicloud.sermant.router.common.utils.ThreadLocalUtils;

import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequest.Builder;
import org.springframework.web.server.ServerWebExchange;

import java.lang.reflect.Method;
import java.util.Optional;
import java.util.function.BiFunction;

/**
 * spring cloud gateway LoadBalancerClientFilter enhancement class， to get the request data
 *
 * @author provenceee
 * @since 2022-07-12
 */
public class LoadBalancerClientFilterInterceptor extends AbstractInterceptor {
    private static final String WRITABLE_HTTP_HEADERS_METHOD_NAME = "writableHttpHeaders";

    private static final String HEADER_FIELD_NAME = "header";

    private final BiFunction<ServerWebExchange, RequestTag, ServerWebExchange> function;

    /**
     * Constructor
     */
    public LoadBalancerClientFilterInterceptor() {
        Optional<Method> method = ReflectUtils
                .findMethod(HttpHeaders.class, WRITABLE_HTTP_HEADERS_METHOD_NAME, new Class[]{HttpHeaders.class});
        if (method.isPresent()) {
            function = this::putHeaders;
        } else {
            function = this::putHeadersByLowerVersionMethod;
        }
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
        ThreadLocalUtils.removeRequestTag();
        ThreadLocalUtils.removeRequestData();
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
        ServerWebExchange newExchange = function.apply(exchange, requestTag);
        context.getArguments()[0] = newExchange;
        return newExchange;
    }

    private ServerWebExchange putHeaders(ServerWebExchange exchange, RequestTag requestTag) {
        HttpHeaders httpHeaders = HttpHeaders.writableHttpHeaders(exchange.getRequest().getHeaders());
        requestTag.getTag().forEach(httpHeaders::putIfAbsent);
        return exchange;
    }

    private ServerWebExchange putHeadersByLowerVersionMethod(ServerWebExchange exchange, RequestTag requestTag) {
        ServerHttpRequest httpRequest = exchange.getRequest();
        HttpHeaders readOnlyHttpHeaders = httpRequest.getHeaders();
        Builder builder = httpRequest.mutate();
        requestTag.getTag().forEach((key, value) -> {
            if (!readOnlyHttpHeaders.containsKey(key)) {
                // Using reflection compatibility with Spring Cloud Finchley.RELEASE
                ReflectUtils.invokeMethod(builder, HEADER_FIELD_NAME, new Class[]{String.class, String.class},
                        new Object[]{key, value.get(0)});
            }
        });
        return exchange.mutate().request(builder.build()).build();
    }
}