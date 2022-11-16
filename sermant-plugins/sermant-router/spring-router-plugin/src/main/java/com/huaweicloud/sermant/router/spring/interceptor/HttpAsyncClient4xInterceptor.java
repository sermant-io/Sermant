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
import com.huaweicloud.sermant.core.utils.StringUtils;
import com.huaweicloud.sermant.router.common.request.RequestData;
import com.huaweicloud.sermant.router.common.utils.FlowContextUtils;
import com.huaweicloud.sermant.router.common.utils.ThreadLocalUtils;

import org.apache.http.HttpRequest;
import org.apache.http.nio.protocol.HttpAsyncRequestProducer;
import org.apache.http.protocol.HttpContext;

import java.util.List;
import java.util.Map;

/**
 * 仅针对4.x版本得http拦截
 *
 * @author yangrh
 * @since 2022-10-31
 */
public class HttpAsyncClient4xInterceptor extends MarkInterceptor {
    private static final int HTTPCONTEXT_INDEX = 2;
    /**
     * 前置触发点
     *
     * @param context 执行上下文
     * @return 执行上下文
     * @throws Exception 执行异常
     */

    @Override
    public ExecuteContext doBefore(ExecuteContext context) throws Exception {
        Object httpAsyncRequestProducerArgument = context.getArguments()[0];
        if (httpAsyncRequestProducerArgument instanceof HttpAsyncRequestProducer) {
            HttpAsyncRequestProducer httpAsyncRequestProducer
                    = (HttpAsyncRequestProducer)httpAsyncRequestProducerArgument;
            HttpRequest httpRequest = httpAsyncRequestProducer.generateRequest();
            Object argument = context.getArguments()[HTTPCONTEXT_INDEX];
            if (argument instanceof HttpContext) {
                HttpContext httpContext = (HttpContext)argument;
                if (StringUtils.isBlank(FlowContextUtils.getTagName())) {
                    return context;
                }
                parseTags(httpContext, httpRequest);
            }
        }
        return context;
    }

    private void parseTags(HttpContext httpContext, HttpRequest httpRequest) {
        Object attribute = httpContext.getAttribute(FlowContextUtils.getTagName());
        if (attribute != null) {
            Map<String, List<String>> map = FlowContextUtils.decodeTags(String.valueOf(attribute));
            if (map != null && map.size() > 0) {
                ThreadLocalUtils.setRequestData(new RequestData(
                        map, httpRequest.getRequestLine().getUri(), httpRequest.getRequestLine().getMethod()));
            }
        }
    }

    /**
     * 后置触发点
     * 说明：该方法后置拦截点不移除线程变量，为了在 NopInstanceFilterInterceptor前置拦截点获取线程变量做流量路由，
     * 在 NopInstanceFilterInterceptor后置拦截点移除线程变量.
     * 使用注意事项：httpasyncclient使用必须有同步线程的future.get()逻辑，否则线程变量无法remove有问题
     *
     * @param context 执行上下文
     * @return 执行上下文
     * @throws Exception 执行异常
     */
    @Override
    public ExecuteContext after(ExecuteContext context) throws Exception {
        return context;
    }

    @Override
    public ExecuteContext onThrow(ExecuteContext context) {
        ThreadLocalUtils.removeRequestData();
        return context;
    }
}
