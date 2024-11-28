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

package io.sermant.router.spring.interceptor;

import io.sermant.core.plugin.agent.entity.ExecuteContext;
import io.sermant.core.plugin.config.PluginConfigManager;
import io.sermant.core.service.xds.entity.ServiceInstance;
import io.sermant.core.utils.LogUtils;
import io.sermant.core.utils.ReflectUtils;
import io.sermant.core.utils.StringUtils;
import io.sermant.router.common.config.RouterConfig;
import io.sermant.router.common.constants.RouterConstant;
import io.sermant.router.common.request.RequestData;
import io.sermant.router.common.utils.FlowContextUtils;
import io.sermant.router.common.utils.ThreadLocalUtils;
import io.sermant.router.spring.utils.BaseHttpRouterUtils;
import okhttp3.Headers;
import okhttp3.HttpUrl;
import okhttp3.Request;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Blocking for okHttp3.x and above versions
 *
 * @author yangrh
 * @since 2022-10-25
 */
public class OkHttp3ClientInterceptor extends MarkInterceptor {
    private static final String FIELD_NAME = "originalRequest";

    private final RouterConfig routerConfig = PluginConfigManager.getPluginConfig(RouterConfig.class);

    /**
     * Pre-trigger point
     *
     * @param context Execution context
     * @return Execution context
     */
    @Override
    public ExecuteContext doBefore(ExecuteContext context) {
        LogUtils.printHttpRequestBeforePoint(context);
        final Optional<Request> rawRequest = getRequest(context);
        if (!rawRequest.isPresent()) {
            return context;
        }
        Request request = rawRequest.get();
        if (handleXdsRouterAndUpdateHttpRequest(context.getObject(), request)) {
            return context;
        }

        URI uri = request.url().uri();
        Headers headers = request.headers();
        if (StringUtils.isBlank(FlowContextUtils.getTagName())) {
            return context;
        }
        String str = headers.get(FlowContextUtils.getTagName());
        Map<String, List<String>> decodeTags = FlowContextUtils.decodeTags(str);
        if (decodeTags.size() > 0) {
            ThreadLocalUtils.setRequestData(new RequestData(decodeTags, uri.getPath(), request.method()));
        }
        return context;
    }

    /**
     * Rear trigger point
     *
     * @param context Execution context
     * @return Execution context
     * @throws Exception Exceptions that were executed
     */
    @Override
    public ExecuteContext after(ExecuteContext context) throws Exception {
        ThreadLocalUtils.removeRequestData();
        LogUtils.printHttpRequestAfterPoint(context);
        return context;
    }

    @Override
    public ExecuteContext onThrow(ExecuteContext context) {
        ThreadLocalUtils.removeRequestData();
        LogUtils.printHttpRequestOnThrowPoint(context);
        return context;
    }

    private Optional<Request> getRequest(ExecuteContext context) {
        final Optional<Object> originalRequest = ReflectUtils.getFieldValue(context.getObject(), FIELD_NAME);
        if (originalRequest.isPresent() && originalRequest.get() instanceof Request) {
            return Optional.of((Request) originalRequest.get());
        }
        return Optional.empty();
    }

    private Map<String, String> getHeaders(Headers headers) {
        Map<String, String> headerMap = new HashMap<>();
        for (String name : headers.names()) {
            headerMap.putIfAbsent(name, headers.get(name));
        }
        return headerMap;
    }

    private Request rebuildRequest(Request request, ServiceInstance serviceInstance) {
        HttpUrl url = request.url().newBuilder()
                .host(serviceInstance.getHost())
                .port(serviceInstance.getPort())
                .build();
        return request.newBuilder()
                .url(url)
                .build();
    }

    private boolean handleXdsRouterAndUpdateHttpRequest(Object obj, Request request) {
        if (!routerConfig.isEnabledXdsRoute()) {
            return false;
        }
        HttpUrl url = request.url();
        String host = url.host();
        String serviceName = host.split(RouterConstant.ESCAPED_POINT)[0];
        if (!BaseHttpRouterUtils.isXdsRouteRequired(serviceName)) {
            return false;
        }

        // use xds route to find a service instance, and modify url by it
        Optional<ServiceInstance> serviceInstanceOptional = BaseHttpRouterUtils
                .chooseServiceInstanceByXds(serviceName, url.encodedPath(),
                        getHeaders(request.headers()));
        if (!serviceInstanceOptional.isPresent()) {
            return false;
        }
        ServiceInstance instance = serviceInstanceOptional.get();
        ReflectUtils.setFieldValue(obj, "originalRequest", rebuildRequest(request, instance));
        return true;
    }
}
