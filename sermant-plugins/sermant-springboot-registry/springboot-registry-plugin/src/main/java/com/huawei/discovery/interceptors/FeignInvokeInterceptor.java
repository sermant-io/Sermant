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

package com.huawei.discovery.interceptors;

import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

import org.apache.http.HttpStatus;

import com.huawei.discovery.entity.Recorder;
import com.huawei.discovery.entity.SimpleRequestRecorder;
import com.huawei.discovery.retry.InvokerContext;
import com.huawei.discovery.service.InvokerService;
import com.huawei.discovery.utils.HttpConstants;
import com.huawei.discovery.utils.PlugEffectWhiteBlackUtils;
import com.huawei.discovery.utils.RequestInterceptorUtils;
import com.huaweicloud.sermant.core.plugin.agent.entity.ExecuteContext;
import com.huaweicloud.sermant.core.plugin.service.PluginServiceManager;

import feign.Request;
import feign.Response;

/**
 * 拦截获取服务列表
 *
 * @author chengyouling
 * @since 2022-9-27
 */
public class FeignInvokeInterceptor extends MarkInterceptor {

    @Override
    protected ExecuteContext doBefore(ExecuteContext context) throws Exception {
        final InvokerService invokerService = PluginServiceManager.getPluginService(InvokerService.class);
        Request request = (Request)context.getArguments()[0];
        Map<String, String> urlInfo = RequestInterceptorUtils.recovertUrl(request.url());
        if (PlugEffectWhiteBlackUtils.isNotAllowRun(request.url(), urlInfo.get(HttpConstants.HTTP_URI_HOST), false)) {
            return context;
        }
        RequestInterceptorUtils.printRequestLog("feign", urlInfo);
        invokerService.invoke(
                buildInvokerFunc(context, request, urlInfo),
                buildExFunc(request),
                urlInfo.get(HttpConstants.HTTP_URI_HOST))
                .ifPresent(context::skip);
        return context;
    }

    private Function<InvokerContext, Object> buildInvokerFunc(ExecuteContext context, Request request, Map<String, String> urlInfo) {
        return invokerContext -> {
            context.getArguments()[0] = Request.create(request.httpMethod(),
                    RequestInterceptorUtils.buildUrl(urlInfo, invokerContext.getServiceInstance()), request.headers(), request.requestBody());
            return RequestInterceptorUtils.buildFunc(context, invokerContext).get();
        };
    }

    private Function<Exception, Object> buildExFunc(Request request) {
        return ex -> buildErrorResponse(ex, request);
    }

    /**
     * 构建feign响应
     * @param ex
     * @return
     */
    private Response buildErrorResponse(Exception ex, Request request) {
        Response.Builder builder = Response.builder();
        builder.status(HttpStatus.SC_INTERNAL_SERVER_ERROR);
        builder.reason(ex.getMessage());
        builder.request(request);
        return builder.build();
    }

    @Override
    public ExecuteContext after(ExecuteContext context) throws Exception {
        return context;
    }

    @Override
    public ExecuteContext onThrow(ExecuteContext context) throws Exception {
        return context;
    }
}
