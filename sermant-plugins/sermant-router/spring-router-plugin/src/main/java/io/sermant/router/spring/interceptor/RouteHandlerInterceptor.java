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

package io.sermant.router.spring.interceptor;

import io.sermant.core.plugin.service.PluginServiceManager;
import io.sermant.router.common.handler.Handler;
import io.sermant.router.common.utils.CollectionUtils;
import io.sermant.router.common.utils.ThreadLocalUtils;
import io.sermant.router.spring.handler.AbstractRequestTagHandler;
import io.sermant.router.spring.handler.AbstractRequestTagHandler.Keys;
import io.sermant.router.spring.handler.LaneRequestTagHandler;
import io.sermant.router.spring.handler.RouteRequestTagHandler;
import io.sermant.router.spring.service.SpringConfigService;

import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Spring Interceptor
 *
 * @author provenceee
 * @since 2022-07-12
 */
public class RouteHandlerInterceptor implements HandlerInterceptor {
    private final List<AbstractRequestTagHandler> handlers;

    private final SpringConfigService configService;

    /**
     * Constructor
     */
    public RouteHandlerInterceptor() {
        configService = PluginServiceManager.getPluginService(SpringConfigService.class);
        handlers = new ArrayList<>();
        handlers.add(new LaneRequestTagHandler());
        handlers.add(new RouteRequestTagHandler());
        handlers.sort(Comparator.comparingInt(Handler::getOrder));
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object obj) {
        Set<String> matchKeys = configService.getMatchKeys();
        Set<String> injectTags = configService.getInjectTags();
        if (CollectionUtils.isEmpty(matchKeys) && CollectionUtils.isEmpty(injectTags)) {
            // The staining mark is empty, which means that there are no staining rules, and it is returned directly
            return true;
        }
        Map<String, List<String>> headers = getHeaders(request);
        Map<String, String[]> parameterMap = request.getParameterMap();
        String path = request.getRequestURI();
        String method = request.getMethod();
        handlers.forEach(handler -> ThreadLocalUtils.addRequestTag(
                handler.getRequestTag(path, method, headers, parameterMap, new Keys(matchKeys, injectTags))));
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object obj,
            ModelAndView modelAndView) {
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object obj, Exception ex) {
        ThreadLocalUtils.removeRequestTag();
    }

    private Map<String, List<String>> getHeaders(HttpServletRequest request) {
        Map<String, List<String>> headers = new HashMap<>();
        Enumeration<?> enumeration = request.getHeaderNames();
        while (enumeration.hasMoreElements()) {
            String key = (String) enumeration.nextElement();
            headers.put(key, enumeration2List(request.getHeaders(key)));
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
}
