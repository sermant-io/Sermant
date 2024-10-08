/*
 * Copyright (C) 2023-2023 Huawei Technologies Co., Ltd. All rights reserved.
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

package io.sermant.discovery.interceptors;

import io.sermant.core.common.LoggerFactory;
import io.sermant.core.plugin.agent.entity.ExecuteContext;
import io.sermant.core.plugin.service.PluginServiceManager;
import io.sermant.core.utils.LogUtils;
import io.sermant.core.utils.ReflectUtils;
import io.sermant.discovery.retry.InvokerContext;
import io.sermant.discovery.service.InvokerService;
import io.sermant.discovery.utils.HttpConstants;
import io.sermant.discovery.utils.PlugEffectWhiteBlackUtils;
import io.sermant.discovery.utils.RequestInterceptorUtils;

import org.apache.hc.client5.http.protocol.HttpClientContext;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.reactive.AbstractClientHttpRequest;

import java.net.URI;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Webclient interception point
 *
 * @author provenceee
 * @since 2023-04-25
 */
public class HttpComponentsClientHttpConnectorInterceptor extends MarkInterceptor {
    private static final Logger LOGGER = LoggerFactory.getLogger();

    private final InvokerService invokerService;

    /**
     * Constructor
     */
    public HttpComponentsClientHttpConnectorInterceptor() {
        invokerService = PluginServiceManager.getPluginService(InvokerService.class);
    }

    @Override
    protected ExecuteContext doBefore(ExecuteContext context) {
        LogUtils.printHttpRequestBeforePoint(context);
        Object[] arguments = context.getArguments();
        AbstractClientHttpRequest request = (AbstractClientHttpRequest) arguments[0];
        Optional<Object> httpRequest = ReflectUtils.getFieldValue(request, "httpRequest");
        if (!httpRequest.isPresent()) {
            return context;
        }
        StringBuilder sb = new StringBuilder();
        ReflectUtils.invokeMethod(httpRequest.get(), "assembleRequestUri", new Class[]{StringBuilder.class},
                new Object[]{sb});
        if (sb.length() == 0) {
            return context;
        }
        String uri = sb.toString();
        Map<String, String> uriInfo = RequestInterceptorUtils.recoverUrl(uri);
        if (!PlugEffectWhiteBlackUtils.isAllowRun(uriInfo.get(HttpConstants.HTTP_URI_HOST),
                uriInfo.get(HttpConstants.HTTP_URI_SERVICE))) {
            return context;
        }
        RequestInterceptorUtils.printRequestLog("webClient(http-client)", uriInfo);
        Optional<Object> result = invokerService.invoke(
                invokerContext -> buildInvokerFunc(context, invokerContext, request, uriInfo),
                ex -> ex,
                uriInfo.get(HttpConstants.HTTP_URI_SERVICE));
        if (result.isPresent()) {
            Object obj = result.get();
            if (obj instanceof Exception) {
                LOGGER.log(Level.SEVERE, "Webclient(http-client) request is error, uri is " + uri, (Exception) obj);
                context.setThrowableOut((Exception) obj);
                return context;
            }
            context.skip(obj);
        }
        return context;
    }

    @Override
    protected void ready() {
    }

    private Object buildInvokerFunc(ExecuteContext context, InvokerContext invokerContext,
            AbstractClientHttpRequest request, Map<String, String> uriInfo) {
        String url = RequestInterceptorUtils.buildUrl(uriInfo, invokerContext.getServiceInstance());
        Optional<Object> httpRequest = ReflectUtils.buildWithConstructor(request.getClass(),
                new Class[]{HttpMethod.class, URI.class, HttpClientContext.class, DataBufferFactory.class},
                new Object[]{request.getMethod(), URI.create(url), context.getArguments()[1], request.bufferFactory()});
        httpRequest.ifPresent(obj -> context.getArguments()[0] = obj);
        return RequestInterceptorUtils.buildFunc(context, invokerContext).get();
    }

    @Override
    public ExecuteContext after(ExecuteContext context) {
        LogUtils.printHttpRequestAfterPoint(context);
        return context;
    }

    @Override
    public ExecuteContext onThrow(ExecuteContext context) {
        LogUtils.printHttpRequestOnThrowPoint(context);
        return context;
    }
}
