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

import com.huawei.discovery.retry.InvokerContext;
import com.huawei.discovery.service.InvokerService;
import com.huawei.discovery.utils.HttpConstants;
import com.huawei.discovery.utils.PlugEffectWhiteBlackUtils;
import com.huawei.discovery.utils.RequestInterceptorUtils;

import com.huaweicloud.sermant.core.common.LoggerFactory;
import com.huaweicloud.sermant.core.plugin.agent.entity.ExecuteContext;
import com.huaweicloud.sermant.core.plugin.service.PluginServiceManager;

import org.springframework.http.HttpMethod;

import java.net.URI;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 拦截获取服务列表
 *
 * @author chengyouling
 * @since 2022-09-27
 */
public class RestTemplateInterceptor extends MarkInterceptor {
    private static final Logger LOGGER = LoggerFactory.getLogger();

    @Override
    protected ExecuteContext doBefore(ExecuteContext context) throws Exception {
        final InvokerService invokerService = PluginServiceManager.getPluginService(InvokerService.class);
        URI uri = (URI) context.getArguments()[0];
        HttpMethod httpMethod = (HttpMethod) context.getArguments()[1];
        if (!PlugEffectWhiteBlackUtils.isHostEqualRealmName(uri.getHost())) {
            return context;
        }
        Map<String, String> hostAndPath = RequestInterceptorUtils.recoverHostAndPath(uri.getPath());
        if (!PlugEffectWhiteBlackUtils.isPlugEffect(hostAndPath.get(HttpConstants.HTTP_URI_SERVICE))) {
            return context;
        }
        RequestInterceptorUtils.printRequestLog("restTemplate", hostAndPath);
        Optional<Object> result = invokerService.invoke(
            buildInvokerFunc(uri, hostAndPath, context, httpMethod),
            ex -> ex,
            hostAndPath.get(HttpConstants.HTTP_URI_SERVICE));
        if (result.isPresent()) {
            Object obj = result.get();
            if (obj instanceof Exception) {
                LOGGER.log(Level.SEVERE, "request is error, uri is " + uri, (Exception) obj);
                context.setThrowableOut((Exception) obj);
                return context;
            }
            context.skip(obj);
        }
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

    private Function<InvokerContext, Object> buildInvokerFunc(URI uri, Map<String, String> hostAndPath,
        ExecuteContext context, HttpMethod httpMethod) {
        return invokerContext -> {
            String url = RequestInterceptorUtils.buildUrlWithIp(uri, invokerContext.getServiceInstance(),
                hostAndPath.get(HttpConstants.HTTP_URI_PATH), httpMethod.name());
            context.getArguments()[0] = rebuildUri(url, uri);
            return RequestInterceptorUtils.buildFunc(context, invokerContext).get();
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
