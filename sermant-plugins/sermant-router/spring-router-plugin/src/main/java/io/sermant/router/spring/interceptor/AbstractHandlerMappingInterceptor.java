/*
 * Copyright (C) 2022-2024 Huawei Technologies Co., Ltd. All rights reserved.
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
import io.sermant.core.plugin.service.PluginServiceManager;
import io.sermant.router.common.handler.Handler;
import io.sermant.router.common.utils.CollectionUtils;
import io.sermant.router.common.utils.ThreadLocalUtils;
import io.sermant.router.spring.entity.Keys;
import io.sermant.router.spring.handler.AbstractHandler;
import io.sermant.router.spring.handler.LaneHandler;
import io.sermant.router.spring.handler.TagHandler;
import io.sermant.router.spring.service.SpringConfigService;
import io.sermant.router.spring.utils.SpringRouterUtils;

import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.server.ServerWebExchange;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Webflux obtains the header interception point
 *
 * @author provenceee
 * @since 2022-10-10
 */
public class AbstractHandlerMappingInterceptor extends AbstractInterceptor {
    private static final String EXCEPT_CLASS_NAME
            = "org.springframework.web.reactive.result.method.annotation.RequestMappingHandlerMapping";

    private final SpringConfigService configService;

    private final List<AbstractHandler> handlers;

    /**
     * Constructor
     */
    public AbstractHandlerMappingInterceptor() {
        configService = PluginServiceManager.getPluginService(SpringConfigService.class);
        handlers = new ArrayList<>();
        handlers.add(new LaneHandler());
        handlers.add(new TagHandler());
        handlers.sort(Comparator.comparingInt(Handler::getOrder));
    }

    @Override
    public ExecuteContext before(ExecuteContext context) {
        if (shouldHandle(context)) {
            ThreadLocalUtils.removeRequestTag();
            Set<String> matchKeys = configService.getMatchKeys();
            Set<String> injectTags = configService.getInjectTags();
            if (CollectionUtils.isEmpty(matchKeys) && CollectionUtils.isEmpty(injectTags)) {
                // The staining mark is empty, which means that there are no staining rules, and it is returned directly
                return context;
            }
            ServerWebExchange exchange = (ServerWebExchange) context.getArguments()[0];
            ServerHttpRequest request = exchange.getRequest();
            HttpHeaders headers = request.getHeaders();
            String path = request.getURI().getPath();
            String methodName = request.getMethod().name();
            String query = request.getURI().getQuery();
            Map<String, List<String>> queryParams = SpringRouterUtils.getParametersByQuery(query);
            handlers.forEach(handler -> ThreadLocalUtils.addRequestTag(
                    handler.getRequestTag(path, methodName, headers, queryParams, new Keys(matchKeys, injectTags))));
        }
        return context;
    }

    @Override
    public ExecuteContext after(ExecuteContext context) {
        // Reactive programming cannot be deleted in the after method, otherwise thread variables cannot be
        // transparently transmitted to the load balancer thread
        // Will be deleted in HttpServerHandleInterceptor, ReactiveTypeHandlerInterceptor
        return context;
    }

    @Override
    public ExecuteContext onThrow(ExecuteContext context) {
        if (shouldHandle(context)) {
            ThreadLocalUtils.removeRequestTag();
        }
        return context;
    }

    private boolean shouldHandle(ExecuteContext context) {
        Object[] arguments = context.getArguments();
        return arguments.length > 0 && arguments[0] instanceof ServerWebExchange
                && EXCEPT_CLASS_NAME.equals(context.getObject().getClass().getName());
    }
}