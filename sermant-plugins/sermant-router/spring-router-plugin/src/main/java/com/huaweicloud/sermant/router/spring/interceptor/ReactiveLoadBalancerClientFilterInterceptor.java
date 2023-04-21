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
import com.huaweicloud.sermant.core.plugin.config.PluginConfigManager;
import com.huaweicloud.sermant.router.common.config.TransmitConfig;
import com.huaweicloud.sermant.router.common.request.RequestData;
import com.huaweicloud.sermant.router.common.request.RequestTag;
import com.huaweicloud.sermant.router.common.utils.ThreadLocalUtils;

import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.server.ServerWebExchange;

/**
 * spring cloud gateway ReactiveLoadBalancerClientFilter增强类，获取请求数据
 *
 * @author provenceee
 * @since 2023-06-07
 */
public class ReactiveLoadBalancerClientFilterInterceptor extends AbstractInterceptor {
    private final TransmitConfig config;

    /**
     * 构造方法
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
        // ReactiveLoadBalancerClientFilter为响应式编程，开启线程池透传时不能在after方法中删除，否则会导致线程变量无法透传到负载均衡线程中
        // 会在HttpServerHandleInterceptor、ReactiveTypeHandlerInterceptor中删除
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