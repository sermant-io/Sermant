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
import com.huaweicloud.sermant.core.utils.LogUtils;
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
 * HTTP interception only for version 4. x
 *
 * @author yangrh
 * @since 2022-10-31
 */
public class HttpAsyncClient4xInterceptor extends MarkInterceptor {
    private static final int HTTPCONTEXT_INDEX = 2;

    /**
     * Pre trigger point
     *
     * @param context Execution context
     * @return Execution context
     * @throws Exception Execution exception
     */
    @Override
    public ExecuteContext doBefore(ExecuteContext context) throws Exception {
        LogUtils.printHttpRequestBeforePoint(context);
        Object httpAsyncRequestProducerArgument = context.getArguments()[0];
        if (httpAsyncRequestProducerArgument instanceof HttpAsyncRequestProducer) {
            HttpAsyncRequestProducer httpAsyncRequestProducer
                    = (HttpAsyncRequestProducer) httpAsyncRequestProducerArgument;
            HttpRequest httpRequest = httpAsyncRequestProducer.generateRequest();
            Object argument = context.getArguments()[HTTPCONTEXT_INDEX];
            if (argument instanceof HttpContext) {
                HttpContext httpContext = (HttpContext) argument;
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
     * Post-Interception Point Note: This method does not remove thread variables from the post-interception point,
     * but obtains thread variables at the NopInstanceFilterInterceptor pre-interceptor point for traffic routing，
     * Remove thread variables from the NopInstanceFilterInterceptor interceptor end.
     * Note: httpasyncclient uses the future.get() logic that must have a synchronous thread, otherwise the thread
     * variable cannot be removed
     *
     * @param context Execution context
     * @return Execution context
     * @throws Exception Execution exception
     */
    @Override
    public ExecuteContext after(ExecuteContext context) throws Exception {
        LogUtils.printHttpRequestAfterPoint(context);
        return context;
    }

    @Override
    public ExecuteContext onThrow(ExecuteContext context) {
        ThreadLocalUtils.removeRequestData();
        LogUtils.printHttpRequestOnThrowPoint(context);
        return context;
    }
}
