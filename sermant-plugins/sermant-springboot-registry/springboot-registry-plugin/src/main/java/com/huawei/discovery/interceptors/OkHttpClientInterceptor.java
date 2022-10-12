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

import java.net.URI;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.logging.Logger;

import org.apache.http.HttpStatus;

import com.huawei.discovery.entity.Recorder;
import com.huawei.discovery.entity.ServiceInstance;
import com.huawei.discovery.entity.SimpleRequestRecorder;
import com.huawei.discovery.retry.InvokerContext;
import com.huawei.discovery.service.InvokerService;
import com.huawei.discovery.utils.HttpConstants;
import com.huawei.discovery.utils.PlugEffectWhiteBlackUtils;
import com.huawei.discovery.utils.RequestInterceptorUtils;
import com.huaweicloud.sermant.core.common.LoggerFactory;
import com.huaweicloud.sermant.core.plugin.agent.entity.ExecuteContext;
import com.huaweicloud.sermant.core.plugin.service.PluginServiceManager;
import com.squareup.okhttp.HttpUrl;
import com.squareup.okhttp.Protocol;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.squareup.okhttp.Response.Builder;

/**
 * 针对okHttp3.1以下版本拦截
 *
 * @author chengyouling
 * @since 2022-09-14
 */
public class OkHttpClientInterceptor extends MarkInterceptor {

    private static final Logger LOGGER = LoggerFactory.getLogger();

    @Override
    protected ExecuteContext doBefore(ExecuteContext context) throws Exception {
        final InvokerService invokerService = PluginServiceManager.getPluginService(InvokerService.class);
        if (context.getRawMemberFieldValue("originalRequest") == null) {
            return context;
        }
        Request request = (Request)context.getRawMemberFieldValue("originalRequest");
        URI uri = request.uri();
        final String method = request.method();
        Map<String, String> hostAndPath = RequestInterceptorUtils.recoverHostAndPath(uri.getPath());
        if (PlugEffectWhiteBlackUtils.isNotAllowRun(uri.getHost(), hostAndPath.get(HttpConstants.HTTP_URI_HOST), true)) {
            return context;
        }
        RequestInterceptorUtils.printRequestLog("OkHttp", hostAndPath);
        AtomicReference<Request> rebuildRequest = new AtomicReference<>();
        invokerService.invoke(
                buildInvokerFunc(uri, hostAndPath, request, method, rebuildRequest, context),
                buildExFunc(rebuildRequest),
                hostAndPath.get(HttpConstants.HTTP_URI_HOST))
                .ifPresent(context::skip);
        return context;
    }

    private Function<Exception, Object> buildExFunc(AtomicReference<Request> rebuildRequest) {
        return ex -> buildErrorResponse(ex, rebuildRequest.get());
    }

    private Function<InvokerContext, Object> buildInvokerFunc(URI uri, Map<String, String> hostAndPath, Request request,
            String method, AtomicReference<Request> rebuildRequest, ExecuteContext context){
        return invokerContext -> {
            Request newRequest = covertRequest(uri, hostAndPath, request, method, invokerContext.getServiceInstance());
            rebuildRequest.set(newRequest);
            try {
                context.setRawMemberFieldValue("originalRequest", newRequest);
            } catch (Exception e) {
                LOGGER.warning("setRawMemberFieldValue originalRequest failed");
                return context;
            }
            return RequestInterceptorUtils.buildFunc(context, invokerContext).get();
        };
    }

    private Request covertRequest(URI uri, Map<String, String> hostAndPath, Request request, String method,
            ServiceInstance serviceInstance) {
        String url = RequestInterceptorUtils.buildUrlWithIp(uri, serviceInstance,
                hostAndPath.get(HttpConstants.HTTP_URI_PATH), method);
        HttpUrl newUrl = HttpUrl.parse(url);
        return request
                .newBuilder()
                .url(newUrl)
                .build();
    }

    /**
     * 构建okHttp响应
     * @param ex
     * @return
     */
    private Response buildErrorResponse(Exception ex, Request request) {
        Response.Builder builder = new Builder();
        builder.code(HttpStatus.SC_INTERNAL_SERVER_ERROR);
        builder.message(ex.getMessage());
        builder.protocol(Protocol.HTTP_1_1);
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
