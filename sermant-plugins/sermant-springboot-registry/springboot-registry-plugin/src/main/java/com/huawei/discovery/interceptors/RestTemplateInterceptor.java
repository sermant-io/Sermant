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

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.logging.Logger;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;

import com.huawei.discovery.entity.Recorder;
import com.huawei.discovery.entity.SimpleRequestRecorder;
import com.huawei.discovery.retry.InvokerContext;
import com.huawei.discovery.service.InvokerService;
import com.huawei.discovery.utils.HttpConstants;
import com.huawei.discovery.utils.PlugEffectWhiteBlackUtils;
import com.huawei.discovery.utils.RequestInterceptorUtils;
import com.huaweicloud.sermant.core.plugin.agent.entity.ExecuteContext;
import com.huaweicloud.sermant.core.plugin.service.PluginServiceManager;

/**
 * 拦截获取服务列表
 *
 * @author chengyouling
 * @since 2022-9-27
 */
public class RestTemplateInterceptor extends MarkInterceptor {

    @Override
    protected ExecuteContext doBefore(ExecuteContext context) throws Exception {
        final InvokerService invokerService = PluginServiceManager.getPluginService(InvokerService.class);
        URI uri = (URI)context.getArguments()[0];
        HttpMethod httpMethod = (HttpMethod)context.getArguments()[1];
        Map<String, String> hostAndPath = RequestInterceptorUtils.recoverHostAndPath(uri.getPath());
        if (PlugEffectWhiteBlackUtils.isNotAllowRun(uri.getHost(), hostAndPath.get(HttpConstants.HTTP_URI_HOST), true)) {
            return context;
        }
        RequestInterceptorUtils.printRequestLog("OkHttp", hostAndPath);
        invokerService.invoke(
                buildInvokerFunc(uri, hostAndPath, context, httpMethod),
                this::buildErrorResponse,
                hostAndPath.get(HttpConstants.HTTP_URI_HOST))
                .ifPresent(context::skip);
        return context;
    }

    private URI rebuildUri(String url, URI uri) {
        final Optional<URI> optionalUri = formatUri(url, uri);
        if (optionalUri.isPresent()) {
            return optionalUri.get();
        }
        throw new IllegalArgumentException("Invalid url: " + url);
    }

    private boolean isValidUrl(String url) {
        final String lowerCaseUrl = url.toLowerCase(Locale.ROOT);
        return lowerCaseUrl.startsWith("http") || lowerCaseUrl.startsWith("https");
    }

    private Optional<URI> formatUri(String url, URI uri) {
        if (!isValidUrl(url)) {
            return Optional.empty();
        }
        return Optional.of(uri.resolve(url));
    }

    private Function<InvokerContext, Object> buildInvokerFunc(URI uri, Map<String, String> hostAndPath, ExecuteContext context, HttpMethod httpMethod) {
        return invokerContext -> {
            String url = RequestInterceptorUtils.buildUrlWithIp(uri, invokerContext.getServiceInstance(),
                    hostAndPath.get(HttpConstants.HTTP_URI_PATH), httpMethod.name());
            context.getArguments()[0] = rebuildUri(url, uri);
            return RequestInterceptorUtils.buildFunc(context, invokerContext).get();
        };
    }

    /**
     * 构建restTemplete响应
     * @param ex
     * @return
     */
    private ClientHttpResponse buildErrorResponse(Exception ex) {
        return new ClientHttpResponse() {

            @Override
            public HttpHeaders getHeaders() {
                return null;
            }

            @Override
            public InputStream getBody() throws IOException {
                return null;
            }

            @Override
            public HttpStatus getStatusCode() throws IOException {
                return HttpStatus.INTERNAL_SERVER_ERROR;
            }

            @Override
            public int getRawStatusCode() throws IOException {
                return HttpStatus.INTERNAL_SERVER_ERROR.value();
            }

            @Override
            public String getStatusText() throws IOException {
                return null;
            }

            @Override
            public void close() {

            }
        };
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
