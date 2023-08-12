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
import com.huaweicloud.sermant.core.service.ServiceManager;
import com.huaweicloud.sermant.router.common.request.RequestHeader;
import com.huaweicloud.sermant.router.common.utils.ThreadLocalUtils;
import com.huaweicloud.sermant.router.spring.service.SpringConfigService;

import org.springframework.http.HttpHeaders;
import org.springframework.web.server.ServerWebExchange;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * webflux获取header拦截点
 *
 * @author provenceee
 * @since 2022-10-10
 */
public class AbstractHandlerMappingInterceptor extends AbstractInterceptor {
    private static final String EXCEPT_CLASS_NAME
        = "org.springframework.web.reactive.result.method.annotation.RequestMappingHandlerMapping";

    private final SpringConfigService configService;

    /**
     * 构造方法
     */
    public AbstractHandlerMappingInterceptor() {
        configService = ServiceManager.getService(SpringConfigService.class);
    }

    @Override
    public ExecuteContext before(ExecuteContext context) {
        if (shouldHandle(context)) {
            ServerWebExchange exchange = (ServerWebExchange) context.getArguments()[0];
            HttpHeaders headers = exchange.getRequest().getHeaders();
            ThreadLocalUtils.setRequestHeader(new RequestHeader(getHeader(headers)));
        }
        return context;
    }

    @Override
    public ExecuteContext after(ExecuteContext context) {
        // 方法会在controller方法之前结束，所以不能在这里释放线程变量，线程变量会在ControllerInterceptor进行释放
        return context;
    }

    @Override
    public ExecuteContext onThrow(ExecuteContext context) {
        if (shouldHandle(context)) {
            ThreadLocalUtils.removeRequestHeader();
        }
        return context;
    }

    private Map<String, List<String>> getHeader(HttpHeaders headers) {
        Map<String, List<String>> map = new HashMap<>();
        Set<String> matchKeys = configService.getMatchKeys();
        for (String headerKey : matchKeys) {
            if (headers.containsKey(headerKey)) {
                map.put(headerKey, headers.get(headerKey));
            }
        }
        return map;
    }

    private boolean shouldHandle(ExecuteContext context) {
        Object[] arguments = context.getArguments();
        return arguments.length > 0 && arguments[0] instanceof ServerWebExchange
            && EXCEPT_CLASS_NAME.equals(context.getObject().getClass().getName());
    }
}