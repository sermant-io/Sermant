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
import com.huaweicloud.sermant.core.plugin.service.PluginServiceManager;
import com.huaweicloud.sermant.core.utils.ReflectUtils;

import java.net.URL;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 拦截HttpUrlConnection#connect方法, 检查SocketTimeoutException: connect timed out
 *
 * @author zhouss
 * @since 2022-10-20
 */
public class HttpUrlConnectionConnectInterceptor extends MarkInterceptor {
    private static final Logger LOGGER = LoggerFactory.getLogger();

    @Override
    protected ExecuteContext doBefore(ExecuteContext context) throws Exception {
        final InvokerService invokerService = PluginServiceManager.getPluginService(InvokerService.class);
        final Optional<URL> rawUrl = getUrl(context.getObject());
        if (!rawUrl.isPresent()) {
            return context;
        }
        final URL url = rawUrl.get();
        final String fullUrl = url.toString();
        Map<String, String> urlInfo = RequestInterceptorUtils.recovertUrl(fullUrl);
        if (!PlugEffectWhiteBlackUtils.isAllowRun(url.getHost(), urlInfo.get(HttpConstants.HTTP_URI_HOST),
            false)) {
            return context;
        }
        HttpConnectionUtils.save(new HttpConnectionContext(urlInfo, url));
        RequestInterceptorUtils.printRequestLog("HttpURLConnection", urlInfo);
        invokerService.invoke(
            buildInvokerFunc(context, url, urlInfo),
            ex -> ex,
            urlInfo.get(HttpConstants.HTTP_URI_HOST))
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
            newUrl.ifPresent(value -> ReflectUtils.setFieldValue(context.getObject(), "url", value));
            return RequestInterceptorUtils.buildFunc(context, invokerContext).get();
        };
    }

    private void tryReleaseConnection(Object target) {
        final Optional<Object> connected = ReflectUtils.getFieldValue(target, "connected");
        if (!connected.isPresent() || connected.get() instanceof Boolean) {
            return;
        }
        final boolean isConnected = (boolean) connected.get();
        if (isConnected) {
            // 释放连接
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
