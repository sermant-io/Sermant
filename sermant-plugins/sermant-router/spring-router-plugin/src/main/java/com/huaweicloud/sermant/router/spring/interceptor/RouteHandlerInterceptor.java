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
import com.huaweicloud.sermant.router.common.request.RequestHeader;
import com.huaweicloud.sermant.router.common.utils.CollectionUtils;
import com.huaweicloud.sermant.router.common.utils.ThreadLocalUtils;
import com.huaweicloud.sermant.router.spring.service.SpringConfigService;

import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
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
    private final SpringConfigService configService;

    /**
     * 构造方法
     */
    public RouteHandlerInterceptor() {
        configService = ServiceManager.getService(SpringConfigService.class);
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object obj) {
        Set<String> matchKeys = configService.getMatchKeys();
        if (CollectionUtils.isEmpty(matchKeys)) {
            return true;
        }
        Collection<String> headerNames = enumeration2Collection(request.getHeaderNames(), false);
        Map<String, List<String>> header = new HashMap<>();
        for (String headerKey : matchKeys) {
            if (headerNames.contains(headerKey)) {
                header.put(headerKey, (List<String>) enumeration2Collection(request.getHeaders(headerKey), true));
            }
        }
        ThreadLocalUtils.setRequestHeader(new RequestHeader(header));
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object obj,
        ModelAndView modelAndView) {
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object obj, Exception ex) {
        ThreadLocalUtils.removeRequestHeader();
    }

    private Collection<String> enumeration2Collection(Enumeration<?> enumeration, boolean isConvertToList) {
        if (enumeration == null) {
            return Collections.emptyList();
        }
        Collection<String> collection = isConvertToList ? new ArrayList<>() : new HashSet<>();
        while (enumeration.hasMoreElements()) {
            collection.add((String) enumeration.nextElement());
        }
        return collection;
    }
}