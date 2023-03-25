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

import com.huawei.discovery.entity.ErrorCloseableHttpResponse;
import com.huawei.discovery.entity.HttpCommonRequest;
import com.huawei.discovery.entity.ServiceInstance;
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

import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.util.EntityUtils;

import java.io.Closeable;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.logging.Level;
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

    private static final String HTTP_POST_NAME = "org.apache.http.client.methods.HttpPost";

    private static final String HTTP_GET_NAME = "org.apache.http.client.methods.HttpGet";

    private static final String HTTP_PUT_NAME = "org.apache.http.client.methods.HttpPut";

    private static final String HTTP_DELETE_NAME = "org.apache.http.client.methods.HttpDelete";

    private static final String HTTP_PATCH_NAME = "org.apache.http.client.methods.HttpPatch";

    private static final String HTTP_HEAD_NAME = "org.apache.http.client.methods.HttpHead";

    private final AtomicBoolean isLoaded = new AtomicBoolean();

    @Override
    public ExecuteContext doBefore(ExecuteContext context) {
        final InvokerService invokerService = PluginServiceManager.getPluginService(InvokerService.class);
        HttpHost httpHost = (HttpHost) context.getArguments()[0];
        final HttpRequest httpRequest = (HttpRequest) context.getArguments()[1];
        if (!PlugEffectWhiteBlackUtils.isHostEqualRealmName(httpHost.getHostName())) {
            return context;
        }
        final Map<String, String> hostAndPath = RequestInterceptorUtils.recoverUrl(httpRequest.getRequestLine()
                .getUri());
        if (hostAndPath.isEmpty()) {
            return context;
        }
        if (!PlugEffectWhiteBlackUtils.isPlugEffect(hostAndPath.get(HttpConstants.HTTP_URI_SERVICE))) {
            return context;
        }
        RequestInterceptorUtils.printRequestLog("HttpClient", hostAndPath);
        invokerService.invoke(
                        buildInvokerFunc(hostAndPath, httpRequest, context),
                        buildExFunc(httpRequest, Thread.currentThread().getContextClassLoader()),
                        hostAndPath.get(HttpConstants.HTTP_URI_SERVICE))
                .ifPresent(result -> this.setResultOrThrow(context, result,
                        hostAndPath.get(HttpConstants.HTTP_URI_PATH)));
        return context;
    }

    private void setResultOrThrow(ExecuteContext context, Object result, String url) {
        if (result instanceof IOException) {
            LOGGER.log(Level.SEVERE, "Http client request failed, uri is " + url, (Exception) result);
            context.setThrowableOut((Exception) result);
            return;
        }
        context.skip(result);
    }

    private Function<InvokerContext, Object> buildInvokerFunc(Map<String, String> hostAndPath, HttpRequest httpRequest,
            ExecuteContext context) {
        final String method = httpRequest.getRequestLine().getMethod();
        final ClassLoader appClassloader = Thread.currentThread().getContextClassLoader();
        final AtomicReference<HttpResponse> lastResult = new AtomicReference<>();
        return invokerContext -> {
            tryClose(lastResult.get());
            final ClassLoader pluginClassloader = Thread.currentThread().getContextClassLoader();
            Thread.currentThread().setContextClassLoader(appClassloader);
            try {
                final ServiceInstance serviceInstance = invokerContext.getServiceInstance();
                String uriNew = RequestInterceptorUtils.buildUrlWithIp(hostAndPath,
                        serviceInstance.getIp(), serviceInstance.getPort());
                final HttpRequest curRequest = rebuildRequest(uriNew, method, httpRequest);
                context.getArguments()[0] = rebuildHttpHost(hostAndPath.get(HttpConstants.HTTP_URL_SCHEME),
                        serviceInstance);
                context.getArguments()[1] = curRequest;
            } finally {
                Thread.currentThread().setContextClassLoader(pluginClassloader);
            }
            final Object result = RequestInterceptorUtils.buildFunc(context, invokerContext).get();
            if (result instanceof HttpResponse) {
                lastResult.set((HttpResponse) result);
            }
            return result;
        };
    }

    private void tryClose(HttpResponse httpResponse) {
        if (httpResponse == null) {
            return;
        }
        try {
            try {
                EntityUtils.consume(httpResponse.getEntity());
            } finally {
                if (httpResponse instanceof Closeable) {
                    ((Closeable) httpResponse).close();
                }
            }
        } catch (IOException ex) {
            // ignored
        }
    }

    private Function<Throwable, Object> buildExFunc(HttpRequest httpRequest, ClassLoader appClassloader) {
        return ex -> {
            if (ex instanceof IOException) {
                return ex;
            }
            final ClassLoader pluginClassloader = Thread.currentThread().getContextClassLoader();
            Thread.currentThread().setContextClassLoader(appClassloader);
            try {
                return new ErrorCloseableHttpResponse(ex, httpRequest.getProtocolVersion());
            } finally {
                Thread.currentThread().setContextClassLoader(pluginClassloader);
            }
        };
    }

    @Override
    protected void ready() {
        if (isLoaded.compareAndSet(false, true)) {
            final ClassLoader classLoader = HttpClient.class.getClassLoader();
            ClassUtils.defineClass(ERROR_RESPONSE_CLASS, classLoader);
            ClassUtils.defineClass(COMMON_REQUEST_CLASS, classLoader);
        }
    }

    private HttpHost rebuildHttpHost(String scheme, ServiceInstance serviceInstance) {
        return new HttpHost(serviceInstance.getHost(), serviceInstance.getPort(), scheme);
    }

    private HttpRequest rebuildRequest(String uriNew, String method, HttpRequest httpUriRequest) {
        switch (httpUriRequest.getClass().getName()) {
            case HTTP_POST_NAME:
                return buildHttpPost(uriNew, (HttpPost) httpUriRequest);
            case HTTP_GET_NAME:
                return buildHttpGet(uriNew, (HttpGet) httpUriRequest);
            case HTTP_PUT_NAME:
                return buildHttpPut(uriNew, (HttpPut) httpUriRequest);
            case HTTP_DELETE_NAME:
                return buildHttpDelete(uriNew, (HttpDelete) httpUriRequest);
            case HTTP_PATCH_NAME:
                return buildHttpPatch(uriNew, (HttpPatch) httpUriRequest);
            case HTTP_HEAD_NAME:
                return buildHttpHead(uriNew, (HttpHead) httpUriRequest);
            default:
                return new HttpCommonRequest(method, uriNew);
        }
    }

    private HttpHead buildHttpHead(String uriNew, HttpHead httpUriRequest) {
        HttpHead httpHead = new HttpHead(uriNew);
        httpHead.setHeaders(httpUriRequest.getAllHeaders());
        httpHead.setConfig(httpUriRequest.getConfig());
        httpHead.setProtocolVersion(httpUriRequest.getProtocolVersion());
        httpHead.setParams(httpUriRequest.getParams());
        return httpHead;
    }

    private HttpPatch buildHttpPatch(String uriNew, HttpPatch httpUriRequest) {
        HttpPatch httpPatch = new HttpPatch(uriNew);
        httpPatch.setHeaders(httpUriRequest.getAllHeaders());
        httpPatch.setConfig(httpUriRequest.getConfig());
        httpPatch.setProtocolVersion(httpUriRequest.getProtocolVersion());
        httpPatch.setParams(httpUriRequest.getParams());
        return httpPatch;
    }

    private HttpDelete buildHttpDelete(String uriNew, HttpDelete httpUriRequest) {
        HttpDelete httpDelete = new HttpDelete(uriNew);
        httpDelete.setHeaders(httpUriRequest.getAllHeaders());
        httpDelete.setConfig(httpUriRequest.getConfig());
        httpDelete.setProtocolVersion(httpUriRequest.getProtocolVersion());
        httpDelete.setParams(httpUriRequest.getParams());
        return httpDelete;
    }

    private HttpPut buildHttpPut(String uriNew, HttpPut httpUriRequest) {
        HttpPut httpPut = new HttpPut(uriNew);
        httpPut.setEntity(httpUriRequest.getEntity());
        httpPut.setHeaders(httpUriRequest.getAllHeaders());
        httpPut.setConfig(httpUriRequest.getConfig());
        httpPut.setProtocolVersion(httpUriRequest.getProtocolVersion());
        httpPut.setParams(httpUriRequest.getParams());
        return httpPut;
    }

    private HttpGet buildHttpGet(String uriNew, HttpGet httpUriRequest) {
        HttpGet httpGet = new HttpGet(uriNew);
        httpGet.setHeaders(httpUriRequest.getAllHeaders());
        httpGet.setConfig(httpUriRequest.getConfig());
        httpGet.setProtocolVersion(httpUriRequest.getProtocolVersion());
        httpGet.setParams(httpUriRequest.getParams());
        return httpGet;
    }

    private HttpPost buildHttpPost(String uriNew, HttpPost httpUriRequest) {
        HttpPost httpPost = new HttpPost(uriNew);
        httpPost.setEntity(httpUriRequest.getEntity());
        httpPost.setHeaders(httpUriRequest.getAllHeaders());
        httpPost.setConfig(httpUriRequest.getConfig());
        httpPost.setProtocolVersion(httpUriRequest.getProtocolVersion());
        httpPost.setParams(httpUriRequest.getParams());
        return httpPost;
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
