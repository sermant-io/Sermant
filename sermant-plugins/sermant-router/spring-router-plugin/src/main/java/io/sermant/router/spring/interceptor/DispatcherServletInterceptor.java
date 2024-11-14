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
import io.sermant.router.spring.handler.AbstractRequestTagHandler;
import io.sermant.router.spring.handler.AbstractRequestTagHandler.Keys;
import io.sermant.router.spring.handler.LaneRequestTagHandler;
import io.sermant.router.spring.handler.RouteRequestTagHandler;
import io.sermant.router.spring.service.SpringConfigService;
import io.sermant.router.spring.utils.SpringRouterUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;

import javax.servlet.http.HttpServletRequest;

/**
 * get http request data
 *
 * @author provenceee
 * @since 2022-07-12
 */
public class DispatcherServletInterceptor extends AbstractInterceptor {
    private final List<AbstractRequestTagHandler> handlers;

    private final SpringConfigService configService;

    private Function<Object, Map<String, String[]>> getParameterMap;

    private Function<Object, String> getRequestUri;

    private Function<Object, String> getMethod;

    private Function<Object, Enumeration<?>> getHeaderNames;

    private BiFunction<Object, String, Enumeration<?>> getHeaders;

    /**
     * Constructor
     */
    public DispatcherServletInterceptor() {
        configService = PluginServiceManager.getPluginService(SpringConfigService.class);
        handlers = new ArrayList<>();
        handlers.add(new LaneRequestTagHandler());
        handlers.add(new RouteRequestTagHandler());
        handlers.sort(Comparator.comparingInt(Handler::getOrder));
        initFunction();
    }

    @Override
    public ExecuteContext before(ExecuteContext context) {
        Set<String> matchKeys = configService.getMatchKeys();
        Set<String> injectTags = configService.getInjectTags();
        if (CollectionUtils.isEmpty(matchKeys) && CollectionUtils.isEmpty(injectTags)) {
            // The staining mark is empty, which means that there are no staining rules, and it is returned directly
            return context;
        }
        Object request = context.getArguments()[0];
        Map<String, List<String>> headers = getHeaders(request);
        Map<String, String[]> parameterMap = getParameterMap.apply(request);
        String path = getRequestUri.apply(request);
        String method = getMethod.apply(request);
        handlers.forEach(handler -> ThreadLocalUtils.addRequestTag(
                handler.getRequestTag(path, method, headers, parameterMap, new Keys(matchKeys, injectTags))));
        return context;
    }

    @Override
    public ExecuteContext after(ExecuteContext context) {
        ThreadLocalUtils.removeRequestData();
        ThreadLocalUtils.removeRequestTag();
        return context;
    }

    @Override
    public ExecuteContext onThrow(ExecuteContext context) {
        ThreadLocalUtils.removeRequestData();
        ThreadLocalUtils.removeRequestTag();
        return context;
    }

    private Map<String, List<String>> getHeaders(Object request) {
        Map<String, List<String>> headers = new HashMap<>();
        Enumeration<?> enumeration = getHeaderNames.apply(request);
        while (enumeration.hasMoreElements()) {
            String key = (String) enumeration.nextElement();
            headers.put(key, enumeration2List(getHeaders.apply(request, key)));
        }
        return headers;
    }

    private List<String> enumeration2List(Enumeration<?> enumeration) {
        if (enumeration == null) {
            return Collections.emptyList();
        }
        List<String> collection = new ArrayList<>();
        while (enumeration.hasMoreElements()) {
            collection.add((String) enumeration.nextElement());
        }
        return collection;
    }

    private void initFunction() {
        boolean canLoadLowVersion = canLoadLowVersion();
        if (canLoadLowVersion) {
            getParameterMap = obj -> ((HttpServletRequest) obj).getParameterMap();
            getRequestUri = obj -> ((HttpServletRequest) obj).getRequestURI();
            getMethod = obj -> ((HttpServletRequest) obj).getMethod();
            getHeaderNames = obj -> ((HttpServletRequest) obj).getHeaderNames();
            getHeaders = (obj, key) -> ((HttpServletRequest) obj).getHeaders(key);
        } else {
            getParameterMap = SpringRouterUtils::getParameterMap;
            getRequestUri = SpringRouterUtils::getRequestUri;
            getMethod = SpringRouterUtils::getMethod;
            getHeaderNames = SpringRouterUtils::getHeaderNames;
            getHeaders = SpringRouterUtils::getHeaders;
        }
    }

    private boolean canLoadLowVersion() {
        try {
            Class.forName(HttpServletRequest.class.getCanonicalName());
        } catch (NoClassDefFoundError | ClassNotFoundException error) {
            return false;
        }
        return true;
    }
}
