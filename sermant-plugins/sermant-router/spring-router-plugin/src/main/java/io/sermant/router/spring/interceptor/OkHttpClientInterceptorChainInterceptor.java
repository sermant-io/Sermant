/*
 * Copyright (C) 2024-2024 Sermant Authors. All rights reserved.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package io.sermant.router.spring.interceptor;

import com.squareup.okhttp.Headers;
import com.squareup.okhttp.Request;

import io.sermant.core.common.LoggerFactory;
import io.sermant.core.plugin.agent.entity.ExecuteContext;
import io.sermant.core.plugin.agent.interceptor.Interceptor;
import io.sermant.core.plugin.config.PluginConfigManager;
import io.sermant.core.service.xds.entity.ServiceInstance;
import io.sermant.router.common.config.RouterConfig;
import io.sermant.router.common.constants.RouterConstant;
import io.sermant.router.spring.utils.BaseHttpRouterUtils;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Intercept for versions below okHttp3.1
 *
 * @author daizhenyu
 * @since 2024-09-06
 */
public class OkHttpClientInterceptorChainInterceptor implements Interceptor {
    private static final Logger LOGGER = LoggerFactory.getLogger();

    private RouterConfig routerConfig = PluginConfigManager.getPluginConfig(RouterConfig.class);

    /**
     * Pre-trigger point
     *
     * @param context Execution context
     * @return Execution context
     * @throws Exception Execution Exception
     */
    @Override
    public ExecuteContext before(ExecuteContext context) throws Exception {
        Object[] arguments = context.getArguments();
        handleXdsRouterAndUpdateHttpRequest(arguments);
        return context;
    }

    /**
     * Rear trigger point
     *
     * @param context Execution context
     * @return Execution context
     * @throws Exception Execution Exception
     */
    @Override
    public ExecuteContext after(ExecuteContext context) throws Exception {
        return context;
    }

    @Override
    public ExecuteContext onThrow(ExecuteContext context) {
        return context;
    }

    private Map<String, String> getHeaders(Request request) {
        Map<String, String> headerMap = new HashMap<>();
        Headers headers = request.headers();
        for (String name : request.headers().names()) {
            headerMap.putIfAbsent(name, headers.get(name));
        }
        return headerMap;
    }

    private Request rebuildRequest(Request request, URI uri, ServiceInstance serviceInstance) {
        URL url = null;
        try {
            url = new URL(BaseHttpRouterUtils.rebuildUrlByXdsServiceInstance(uri, serviceInstance));
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Convert url string to url failed.", e.getMessage());
            return request;
        }
        return request.newBuilder()
                .url(url)
                .build();
    }

    private void handleXdsRouterAndUpdateHttpRequest(Object[] arguments) {
        if (!routerConfig.isEnabledXdsRoute()) {
            return;
        }
        Request request = (Request) arguments[0];
        URI uri = null;
        try {
            uri = request.uri();
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Get uri from okhttp request failed.", e.getMessage());
            return;
        }
        String host = uri.getHost();
        if (!BaseHttpRouterUtils.isXdsRouteRequired(host)) {
            return;
        }

        // use xds route to find a service instance, and modify url by it
        Optional<ServiceInstance> serviceInstanceOptional = BaseHttpRouterUtils
                .chooseServiceInstanceByXds(host.split(RouterConstant.ESCAPED_POINT)[0], uri.getPath(),
                        getHeaders(request));
        if (!serviceInstanceOptional.isPresent()) {
            return;
        }
        ServiceInstance instance = serviceInstanceOptional.get();
        arguments[0] = rebuildRequest(request, uri, instance);
    }
}
