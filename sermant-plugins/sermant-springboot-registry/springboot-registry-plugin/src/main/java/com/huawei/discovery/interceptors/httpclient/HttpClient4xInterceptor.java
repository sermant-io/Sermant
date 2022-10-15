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

package com.huawei.discovery.interceptors.httpclient;

import com.huawei.discovery.entity.SimpleRequestRecorder;
import com.huawei.discovery.interceptors.MarkInterceptor;
import com.huawei.discovery.retry.InvokerContext;
import com.huawei.discovery.service.InvokerService;
import com.huawei.discovery.utils.HttpConstants;
import com.huawei.discovery.utils.PlugEffectWhiteBlackUtils;
import com.huawei.discovery.utils.RequestInterceptorUtils;

import com.huaweicloud.sermant.core.common.LoggerFactory;
import com.huaweicloud.sermant.core.plugin.agent.entity.ExecuteContext;
import com.huaweicloud.sermant.core.plugin.service.PluginServiceManager;
import com.huaweicloud.sermant.core.utils.ClassUtils;
import com.huaweicloud.sermant.core.utils.ReflectUtils;

import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.ProtocolVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIUtils;

import java.net.URI;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.logging.Logger;

/**
 * 仅针对4.x版本得http拦截
 *
 * @author zhouss
 * @since 2022-10-10
 */
public class HttpClient4xInterceptor extends MarkInterceptor {
    private static final Logger LOGGER = LoggerFactory.getLogger();

    private static final String ERROR_RESPONSE_CLASS = "com.huawei.discovery.entity.ErrorCloseableHttpResponse";

    private static final String COMMON_REQUEST_CLASS = "com.huawei.discovery.entity.HttpCommonRequest";

    private final AtomicBoolean isLoaded = new AtomicBoolean();

    private final SimpleRequestRecorder requestRecorder = new SimpleRequestRecorder();

    @Override
    public ExecuteContext doBefore(ExecuteContext context) {
        final InvokerService invokerService = PluginServiceManager.getPluginService(InvokerService.class);
        HttpHost httpHost = (HttpHost) context.getArguments()[0];
        final HttpRequest httpRequest = (HttpRequest) context.getArguments()[1];
        final Optional<URI> optionalUri = RequestInterceptorUtils.formatUri(httpRequest.getRequestLine().getUri());
        if (!optionalUri.isPresent()) {
            return context;
        }
        URI uri = optionalUri.get();
        Map<String, String> hostAndPath = RequestInterceptorUtils.recoverHostAndPath(uri.getPath());
        if (!isConfigEnable(hostAndPath, httpHost.getHostName())) {
            return context;
        }
        RequestInterceptorUtils.printRequestLog("HttpClient", hostAndPath);
        requestRecorder.beforeRequest();
        invokerService.invoke(
                buildInvokerFunc(hostAndPath, uri, httpRequest, context),
                buildExFunc(httpRequest),
                hostAndPath.get(HttpConstants.HTTP_URI_HOST))
                .ifPresent(context::skip);
        return context;
    }

    private Function<InvokerContext, Object> buildInvokerFunc(Map<String, String> hostAndPath, URI uri,
            HttpRequest httpRequest, ExecuteContext context) {
        final String method = httpRequest.getRequestLine().getMethod();
        final ClassLoader appClassloader = Thread.currentThread().getContextClassLoader();
        return invokerContext -> {
            final ClassLoader pluginClassloader = Thread.currentThread().getContextClassLoader();
            Thread.currentThread().setContextClassLoader(appClassloader);
            try {
                String uriNew = RequestInterceptorUtils.buildUrlWithIp(uri, invokerContext.getServiceInstance(),
                        hostAndPath.get(HttpConstants.HTTP_URI_PATH), method);
                final HttpRequest curRequest = rebuildRequest(uriNew, method, httpRequest);
                if (curRequest == null) {
                    LOGGER.warning(String.format(Locale.ENGLISH,
                            "Can not rebuild request for replaced url [%s], it will not change origin url", uriNew));
                    return RequestInterceptorUtils.buildFunc(context, invokerContext).get();
                }
                context.getArguments()[1] = curRequest;
                context.getArguments()[0] = rebuildHttpHost(uriNew);
            } finally {
                Thread.currentThread().setContextClassLoader(pluginClassloader);
            }
            return RequestInterceptorUtils.buildFunc(context, invokerContext).get();
        };
    }

    private Function<Exception, Object> buildExFunc(HttpRequest httpRequest) {
        final ClassLoader appClassloader = Thread.currentThread().getContextClassLoader();
        return ex -> {
            final ClassLoader pluginClassloader = Thread.currentThread().getContextClassLoader();
            Thread.currentThread().setContextClassLoader(appClassloader);
            try {
                return ReflectUtils.buildWithConstructor(
                        "com.huawei.discovery.entity.ErrorCloseableHttpResponse",
                        new Class[] {Exception.class, ProtocolVersion.class},
                        new Object[] {ex, httpRequest.getProtocolVersion()})
                        .orElse(null);
            } finally {
                Thread.currentThread().setContextClassLoader(pluginClassloader);
            }
        };
    }

    private boolean isConfigEnable(Map<String, String> hostAndPath, String hostName) {
        return PlugEffectWhiteBlackUtils.isAllowRun(hostName, hostAndPath.get(HttpConstants.HTTP_URI_HOST), true);
    }

    @Override
    protected void ready() {
        if (isLoaded.compareAndSet(false, true)) {
            final ClassLoader classLoader = HttpClient.class.getClassLoader();
            ClassUtils.defineClass(ERROR_RESPONSE_CLASS, classLoader);
            ClassUtils.defineClass(COMMON_REQUEST_CLASS, classLoader);
        }
    }

    private HttpHost rebuildHttpHost(String uriNew) {
        final Optional<URI> optionalUri = RequestInterceptorUtils.formatUri(uriNew);
        if (optionalUri.isPresent()) {
            return URIUtils.extractHost(optionalUri.get());
        }
        throw new IllegalArgumentException("Invalid url: " + uriNew);
    }

    private HttpRequest rebuildRequest(String uriNew, String method, HttpRequest httpUriRequest) {
        if (httpUriRequest instanceof HttpPost) {
            HttpPost oldHttpPost = (HttpPost) httpUriRequest;
            HttpPost httpPost = new HttpPost(uriNew);
            httpPost.setEntity(oldHttpPost.getEntity());
            return httpPost;
        } else {
            final Optional<Object> result = ReflectUtils
                    .buildWithConstructor(COMMON_REQUEST_CLASS,
                            new Class[]{String.class, String.class}, new Object[]{method, uriNew});
            return (HttpRequest) result.orElse(null);
        }
    }

    @Override
    public ExecuteContext after(ExecuteContext context) throws Exception {
        return context;
    }

    @Override
    public ExecuteContext onThrow(ExecuteContext context) {
        return context;
    }
}
