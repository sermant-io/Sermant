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

package com.huawei.discovery.interceptors.httpconnection;

import com.huawei.discovery.config.LbConfig;
import com.huawei.discovery.retry.InvokerContext;
import com.huawei.discovery.service.InvokerService;
import com.huawei.discovery.utils.HttpConnectionUtils;
import com.huawei.discovery.utils.HttpConnectionUtils.HttpConnectionContext;
import com.huawei.discovery.utils.HttpConstants;
import com.huawei.discovery.utils.RequestInterceptorUtils;

import com.huaweicloud.sermant.core.common.LoggerFactory;
import com.huaweicloud.sermant.core.plugin.agent.entity.ExecuteContext;
import com.huaweicloud.sermant.core.plugin.agent.interceptor.Interceptor;
import com.huaweicloud.sermant.core.plugin.config.PluginConfigManager;
import com.huaweicloud.sermant.core.plugin.service.PluginServiceManager;
import com.huaweicloud.sermant.core.utils.ReflectUtils;

import sun.net.www.http.HttpClient;
import sun.net.www.protocol.http.HttpURLConnection;

import java.io.Closeable;
import java.io.IOException;
import java.net.Proxy;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 拦截HttpUrlConnection#getInputSteam方法, 该拦截主要使之可检测到readTimeOut异常, connect timed out 见connect方法拦截点
 *
 * @author zhouss
 * @since 2022-10-20
 */
public class HttpUrlConnectionResponseStreamInterceptor implements Interceptor {
    private static final Logger LOGGER = LoggerFactory.getLogger();

    private final LbConfig lbConfig;

    /**
     * 构造器, 初始化配置
     */
    public HttpUrlConnectionResponseStreamInterceptor() {
        this.lbConfig = PluginConfigManager.getPluginConfig(LbConfig.class);
    }

    @Override
    public ExecuteContext before(ExecuteContext context) throws Exception {
        final HttpConnectionContext connectionContext = HttpConnectionUtils.getContext();
        if (connectionContext == null) {
            return context;
        }
        final URL url = connectionContext.getOriginUrl();
        final Map<String, String> urlInfo = connectionContext.getUrlInfo();
        HttpConnectionUtils.remove();
        final InvokerService invokerService = PluginServiceManager.getPluginService(InvokerService.class);
        final Function<InvokerContext, Object> invokerContextObjectFunction = buildInvokerFunc(context, url, urlInfo);
        final InvokerContext invokerContext = new InvokerContext();
        final Object rawInputStream = invokerContextObjectFunction.apply(invokerContext);
        if (!isNeedRetry(invokerContext.getEx())) {
            return context;
        }
        tryCloseOldInputStream(rawInputStream);
        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.log(Level.FINE, String.format(Locale.ENGLISH, "invoke method [%s] failed",
                    context.getMethod().getName()), invokerContext.getEx());
        }
        Optional<Object> result = invokerService.invoke(
            invokerContextObjectFunction,
            ex -> ex,
            urlInfo.get(HttpConstants.HTTP_URI_SERVICE));
        if (result.isPresent()) {
            Object obj = result.get();
            if (obj instanceof Exception) {
                LOGGER.log(Level.SEVERE, "[HttpUrlConnection]request is error, uri is " + url, (Exception) obj);
                context.setThrowableOut((Exception) obj);
                return context;
            }
        }
        return context;
    }

    private void tryCloseOldInputStream(Object rawInputStream) {
        if (rawInputStream instanceof Closeable) {
            // 针对旧的输入流进行关闭处理
            try {
                ((Closeable) rawInputStream).close();
            } catch (IOException e) {
                LOGGER.warning("Close old input stream failed when invoke");
            }
        }
    }

    private boolean isNeedRetry(Throwable ex) {
        if (ex instanceof SocketTimeoutException) {
            // 此处仅SocketTimeoutException: Read timed out进行重试
            final String message = ex.getMessage();
            return "Read timed out".equalsIgnoreCase(message) && isEnableRetry();
        }
        return false;
    }

    private boolean isEnableRetry() {
        return this.lbConfig.isEnableSocketReadTimeoutRetry() && this.lbConfig.getMaxRetry() > 0;
    }

    private void resetStats(ExecuteContext context) {
        ReflectUtils.setFieldValue(context.getObject(), "rememberedException", null);
        ReflectUtils.setFieldValue(context.getObject(), "failedOnce", false);
        ReflectUtils.setFieldValue(context.getObject(), "responseCode", -1);
    }

    private Function<InvokerContext, Object> buildInvokerFunc(ExecuteContext context, URL url,
            Map<String, String> urlInfo) {
        final AtomicBoolean isFirstInvoke = new AtomicBoolean();
        final AtomicReference<Object> lastResult = new AtomicReference<>();
        return invokerContext -> {
            tryCloseOldInputStream(lastResult.get());
            if (!isFirstInvoke.get()) {
                isFirstInvoke.set(true);
            } else {
                resetStats(context);
                final String path = urlInfo.get(HttpConstants.HTTP_URI_PATH);
                final Optional<URL> newUrl = RequestInterceptorUtils.rebuildUrlForHttpConnection(url,
                        invokerContext.getServiceInstance(), path);
                newUrl.ifPresent(value -> ReflectUtils.setFieldValue(context.getObject(), "url", value));
                newUrl.ifPresent(value -> resetHttpClient(context.getObject(), value));
            }
            final Object result = RequestInterceptorUtils.buildFunc(context, invokerContext).get();
            lastResult.set(result);
            return result;
        };
    }

    private void resetHttpClient(Object target, URL url) {
        final Optional<Object> http = ReflectUtils.getFieldValue(target, "http");
        if (http.isPresent() && http.get() instanceof HttpClient) {
            // 关闭原始的httpclient
            ((HttpClient) http.get()).closeServer();
        }
        final Optional<Object> connectTimeout = ReflectUtils.getFieldValue(target, "connectTimeout");
        final Optional<Object> readTimeout = ReflectUtils.getFieldValue(target, "readTimeout");
        if (!connectTimeout.isPresent() || !readTimeout.isPresent()) {
            return;
        }
        final Optional<Object> instProxy = ReflectUtils.getFieldValue(target, "instProxy");
        try {
            HttpClient newClient;
            if (instProxy.isPresent() && instProxy.get() instanceof Proxy) {
                newClient = HttpClient.New(url, (Proxy) instProxy.get(), (int) connectTimeout.get(), true,
                        (HttpURLConnection) target);
            } else {
                newClient = HttpClient
                        .New(url, null, (int) connectTimeout.get(), true, (HttpURLConnection) target);
            }
            newClient.setReadTimeout((int) readTimeout.get());
            ReflectUtils.setFieldValue(target, "http", newClient);
        } catch (IOException e) {
            LOGGER.info("Can not create httpclient when invoke!");
        }
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
