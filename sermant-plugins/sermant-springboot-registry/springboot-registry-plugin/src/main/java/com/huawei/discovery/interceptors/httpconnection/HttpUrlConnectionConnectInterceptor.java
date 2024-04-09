/*
 * Copyright (C) 2022-2024 Sermant Authors. All rights reserved.
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
import com.huawei.discovery.interceptors.MarkInterceptor;
import com.huawei.discovery.retry.InvokerContext;
import com.huawei.discovery.service.InvokerService;
import com.huawei.discovery.utils.HttpConnectionUtils;
import com.huawei.discovery.utils.HttpConnectionUtils.HttpConnectionContext;
import com.huawei.discovery.utils.HttpConstants;
import com.huawei.discovery.utils.PlugEffectWhiteBlackUtils;
import com.huawei.discovery.utils.RequestInterceptorUtils;

import com.huaweicloud.sermant.core.common.LoggerFactory;
import com.huaweicloud.sermant.core.plugin.agent.entity.ExecuteContext;
import com.huaweicloud.sermant.core.plugin.config.PluginConfigManager;
import com.huaweicloud.sermant.core.plugin.service.PluginServiceManager;
import com.huaweicloud.sermant.core.utils.ReflectUtils;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Proxy.Type;
import java.net.URL;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Intercept the HttpUrlConnection#connect method and check for SocketTimeoutException: connect timed out
 *
 * @author zhouss
 * @since 2022-10-20
 */
public class HttpUrlConnectionConnectInterceptor extends MarkInterceptor {
    private static final Logger LOGGER = LoggerFactory.getLogger();

    /**
     * Proxy caching, for cases where a user is using a proxy key: host value: Proxy
     */
    private final Map<String, Proxy> proxyCache;

    private final LbConfig lbConfig;

    /**
     * Constructor
     */
    public HttpUrlConnectionConnectInterceptor() {
        this.lbConfig = PluginConfigManager.getPluginConfig(LbConfig.class);
        if (this.lbConfig.isEnableCacheProxy()) {
            proxyCache = new ConcurrentHashMap<>();
        } else {
            proxyCache = null;
        }
    }

    @Override
    protected ExecuteContext doBefore(ExecuteContext context) throws Exception {
        final InvokerService invokerService = PluginServiceManager.getPluginService(InvokerService.class);
        final Optional<URL> rawUrl = getUrl(context.getObject());
        if (!rawUrl.isPresent()) {
            return context;
        }
        final URL url = rawUrl.get();
        if (!PlugEffectWhiteBlackUtils.isHostEqualRealmName(url.getHost())) {
            return context;
        }
        final String fullUrl = url.toString();
        Map<String, String> urlInfo = RequestInterceptorUtils.recoverUrl(url);
        if (!PlugEffectWhiteBlackUtils.isPlugEffect(urlInfo.get(HttpConstants.HTTP_URI_SERVICE))) {
            return context;
        }
        HttpConnectionUtils.save(new HttpConnectionContext(urlInfo, url));
        RequestInterceptorUtils.printRequestLog("HttpURLConnection", urlInfo);
        invokerService.invoke(
                        buildInvokerFunc(context, url, urlInfo),
                        ex -> ex,
                        urlInfo.get(HttpConstants.HTTP_URI_SERVICE))
                .ifPresent(obj -> {
                    if (obj instanceof Exception) {
                        LOGGER.log(Level.SEVERE, "request is error, uri is " + fullUrl, (Exception) obj);
                        context.setThrowableOut((Exception) obj);
                        return;
                    }
                    context.skip(obj);
                });
        return context;
    }

    @Override
    protected void ready() {
    }

    private Optional<URL> getUrl(Object target) {
        final Optional<Object> url = ReflectUtils.getFieldValue(target, "url");
        if (url.isPresent() && url.get() instanceof URL) {
            return Optional.of((URL) url.get());
        }
        return Optional.empty();
    }

    private Function<InvokerContext, Object> buildInvokerFunc(ExecuteContext context, URL url,
            Map<String, String> urlInfo) {
        return invokerContext -> {
            tryReleaseConnection(context.getObject());
            final String path = urlInfo.get(HttpConstants.HTTP_URI_PATH);
            final Optional<URL> newUrl = RequestInterceptorUtils.rebuildUrlForHttpConnection(url,
                    invokerContext.getServiceInstance(), path);
            newUrl.ifPresent(value -> {
                ReflectUtils.setFieldValue(context.getObject(), "url", value);
                tryResetProxy(value, context);
            });
            return RequestInterceptorUtils.buildFunc(context, invokerContext).get();
        };
    }

    /**
     * In the scenario of specifying a proxy, you need to replace the address of the proxy with the actual downstream
     * address; otherwise 404 will appear
     *
     * @param newUrl Actual downstream address
     * @param context Interceptor context
     */
    private void tryResetProxy(URL newUrl, ExecuteContext context) {
        final Optional<Object> instProxy = ReflectUtils.getFieldValue(context.getObject(), "instProxy");
        if (!instProxy.isPresent() || !(instProxy.get() instanceof Proxy)) {
            return;
        }
        final Proxy proxy = (Proxy) instProxy.get();
        if (proxy.type() != Type.HTTP) {
            return;
        }

        // The user uses its own proxy to replace the resolved downstream address
        ReflectUtils.setFieldValue(context.getObject(), "instProxy", getProxy(newUrl));
    }

    private Proxy getProxy(URL newUrl) {
        if (lbConfig.isEnableCacheProxy()) {
            return proxyCache.computeIfAbsent(newUrl.getHost(), host -> createProxy(newUrl));
        }
        return createProxy(newUrl);
    }

    private Proxy createProxy(URL newUrl) {
        return new Proxy(Type.HTTP, InetSocketAddress.createUnresolved(newUrl.getHost(), newUrl.getPort()));
    }

    private void tryReleaseConnection(Object target) {
        final Optional<Object> connected = ReflectUtils.getFieldValue(target, "connected");
        if (!connected.isPresent() || connected.get() instanceof Boolean) {
            return;
        }
        final boolean isConnected = (boolean) connected.get();
        if (isConnected) {
            // Release the connection
            LOGGER.fine("Release Http url connection when read timed out for retry!");
            ReflectUtils.invokeMethod(target, "disconnect", null, null);
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
