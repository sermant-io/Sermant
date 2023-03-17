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

import com.huaweicloud.sermant.core.service.ServiceManager;
import com.huaweicloud.sermant.router.common.handler.Handler;
import com.huaweicloud.sermant.router.common.utils.CollectionUtils;
import com.huaweicloud.sermant.router.common.utils.ThreadLocalUtils;
import com.huaweicloud.sermant.router.spring.handler.AbstractRequestTagHandler;
import com.huaweicloud.sermant.router.spring.handler.AbstractRequestTagHandler.Keys;
import com.huaweicloud.sermant.router.spring.handler.LaneRequestTagHandler;
import com.huaweicloud.sermant.router.spring.handler.RouteRequestTagHandler;
import com.huaweicloud.sermant.router.spring.service.SpringConfigService;

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
 * spring拦截器
 *
 * @author provenceee
 * @since 2022-07-12
 */
public class RouteHandlerInterceptor implements HandlerInterceptor {
    private final List<AbstractRequestTagHandler> handlers;

    private final SpringConfigService configService;

    /**
     * 构造方法
     */
    public RouteHandlerInterceptor() {
        configService = ServiceManager.getService(SpringConfigService.class);
        handlers = new ArrayList<>();
        handlers.add(new LaneRequestTagHandler());
        handlers.add(new RouteRequestTagHandler());
        handlers.sort(Comparator.comparingInt(Handler::getOrder));
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object obj) {
        Set<String> matchKeys = configService.getMatchKeys();
        Set<String> matchTags = configService.getMatchTags();
        if (CollectionUtils.isEmpty(matchKeys) && CollectionUtils.isEmpty(matchTags)) {
            // 染色标记为空，代表没有染色规则，直接return
            return true;
        }
        Map<String, List<String>> headers = getHeaders(request);
        Map<String, String[]> parameterMap = request.getParameterMap();
        String path = request.getRequestURI();
        String method = request.getMethod();
        handlers.forEach(handler -> ThreadLocalUtils.addRequestTag(
                handler.getRequestTag(path, method, headers, parameterMap, new Keys(matchKeys, matchTags))));
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