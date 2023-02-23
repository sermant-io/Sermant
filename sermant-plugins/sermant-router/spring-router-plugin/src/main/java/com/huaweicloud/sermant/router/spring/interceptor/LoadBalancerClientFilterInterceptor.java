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
import com.huaweicloud.sermant.router.common.request.RequestData;
import com.huaweicloud.sermant.router.common.request.RequestTag;
import com.huaweicloud.sermant.router.common.utils.ThreadLocalUtils;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.web.server.ServerWebExchange;

/**
 * spring cloud gateway LoadBalancerClientFilter增强类，获取请求数据
 *
 * @author provenceee
 * @since 2022-07-12
 */
public class LoadBalancerClientFilterInterceptor extends AbstractInterceptor {
    @Override
    public ExecuteContext before(ExecuteContext context) {
        Object argument = context.getArguments()[0];
        if (argument instanceof ServerWebExchange) {
            ServerWebExchange exchange = (ServerWebExchange) argument;
            HttpRequest request = exchange.getRequest();
            HttpHeaders headers = request.getHeaders();
            putHeaders(headers);
            String path = request.getURI().getPath();
            ThreadLocalUtils.setRequestData(new RequestData(headers, path, request.getMethod().name()));
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

    private void putHeaders(HttpHeaders readOnlyHttpHeaders) {
        HttpHeaders httpHeaders = HttpHeaders.writableHttpHeaders(readOnlyHttpHeaders);
        RequestTag requestTag = ThreadLocalUtils.getRequestTag();
        if (requestTag != null) {
            requestTag.getTag().forEach(httpHeaders::putIfAbsent);
        }
    }
}