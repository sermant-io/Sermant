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

import com.huawei.discovery.entity.ServiceInstance;
import com.huawei.discovery.retry.InvokerContext;
import com.huawei.discovery.service.InvokerService;
import com.huawei.discovery.utils.HttpConstants;
import com.huawei.discovery.utils.PlugEffectWhiteBlackUtils;
import com.huawei.discovery.utils.RequestInterceptorUtils;

import com.huaweicloud.sermant.core.common.LoggerFactory;
import com.huaweicloud.sermant.core.plugin.agent.entity.ExecuteContext;
import com.huaweicloud.sermant.core.plugin.service.PluginServiceManager;

import okhttp3.HttpUrl;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Response.Builder;

import org.apache.http.HttpStatus;

import java.net.URI;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.logging.Logger;

/**
 * 针对okHttp3.x版本以上的拦截
 *
 * @author chengyouling
 * @since 2022-09-14
 */
public class OkHttp3ClientInterceptor extends MarkInterceptor {

    private static final Logger LOGGER = LoggerFactory.getLogger();

    private static final String FIELD_NAME = "originalRequest";

    @Override
    protected ExecuteContext doBefore(ExecuteContext context) throws Exception {
        final InvokerService invokerService = PluginServiceManager.getPluginService(InvokerService.class);
        if (context.getRawMemberFieldValue(FIELD_NAME) == null) {
            return context;
        }
        Request request = (Request)context.getRawMemberFieldValue(FIELD_NAME);
        URI uri = request.url().uri();
        Map<String, String> hostAndPath = RequestInterceptorUtils.recoverHostAndPath(uri.getPath());
        if (!PlugEffectWhiteBlackUtils.isAllowRun(uri.getHost(), hostAndPath.get(HttpConstants.HTTP_URI_HOST),
            true)) {
            return context;
        }
        RequestInterceptorUtils.printRequestLog("OkHttp3", hostAndPath);
        AtomicReference<Request> rebuildRequest = new AtomicReference<>();
        invokerService.invoke(
                buildInvokerFunc(uri, hostAndPath, request, rebuildRequest, context),
                buildExFunc(rebuildRequest),
                hostAndPath.get(HttpConstants.HTTP_URI_HOST))
                .ifPresent(context::skip);
        return context;
    }

    private Function<Exception, Object> buildExFunc(AtomicReference<Request> rebuildRequest) {
        return ex -> buildErrorResponse(ex, rebuildRequest.get());
    }

    private Function<InvokerContext, Object> buildInvokerFunc(URI uri, Map<String, String> hostAndPath, Request request,
            AtomicReference<Request> rebuildRequest, ExecuteContext context) {
        return invokerContext -> {
            final String method = request.method();
            Request newRequest = covertRequest(uri, hostAndPath, request, method, invokerContext.getServiceInstance());
            rebuildRequest.set(newRequest);
            try {
                context.setRawMemberFieldValue(FIELD_NAME, newRequest);
            } catch (NoSuchFieldException e) {
                LOGGER.warning("setRawMemberFieldValue originalRequest failed");
                return context;
            } catch (IllegalAccessException e) {
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
     * 构建okHttp3响应
     *
     * @param ex
     * @return 响应
     */
    private Response buildErrorResponse(Exception ex, Request request) {
        Builder builder = new Builder();
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
