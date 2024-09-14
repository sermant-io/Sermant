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

import io.sermant.core.common.LoggerFactory;
import io.sermant.core.plugin.agent.entity.ExecuteContext;
import io.sermant.core.plugin.config.PluginConfigManager;
import io.sermant.core.service.xds.entity.ServiceInstance;
import io.sermant.core.utils.LogUtils;
import io.sermant.core.utils.StringUtils;
import io.sermant.router.common.config.RouterConfig;
import io.sermant.router.common.request.RequestData;
import io.sermant.router.common.utils.CollectionUtils;
import io.sermant.router.common.utils.FlowContextUtils;
import io.sermant.router.common.utils.ThreadLocalUtils;
import io.sermant.router.spring.utils.BaseHttpRouterUtils;

import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.client.methods.HttpRequestBase;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * HTTP interception only for version 4. x
 *
 * @author yangrh
 * @since 2022-10-25
 */
public class HttpClient4xInterceptor extends MarkInterceptor {
    private static final Logger LOGGER = LoggerFactory.getLogger();

    private RouterConfig routerConfig = PluginConfigManager.getPluginConfig(RouterConfig.class);

    /**
     * Pre-trigger point
     *
     * @param context Execution context
     * @return Execution context
     * @throws Exception Execution exception
     */
    @Override
    public ExecuteContext doBefore(ExecuteContext context) throws Exception {
        LogUtils.printHttpRequestBeforePoint(context);
        Object[] arguments = context.getArguments();

        Object httpRequestObject = arguments[1];
        if (!(httpRequestObject instanceof HttpRequestBase)) {
            return context;
        }
        final HttpRequestBase httpRequest = (HttpRequestBase) httpRequestObject;

        handleXdsRouterAndUpdateHttpRequest(arguments);

        if (StringUtils.isBlank(FlowContextUtils.getTagName())) {
            return context;
        }
        Header[] headers = httpRequest.getHeaders(FlowContextUtils.getTagName());
        Map<String, List<String>> flowTags = new HashMap<>();
        if (headers == null || headers.length == 0) {
            return context;
        }
        for (Header header : headers) {
            String headerValue = header.getValue();
            Map<String, List<String>> stringListMap = FlowContextUtils.decodeTags(headerValue);
            flowTags.putAll(stringListMap);
        }
        if (CollectionUtils.isEmpty(flowTags)) {
            return context;
        }
        ThreadLocalUtils.setRequestData(new RequestData(
                flowTags, httpRequest.getURI().getPath(), httpRequest.getRequestLine().getMethod()));
        return context;
    }

    /**
     * Rear trigger point
     *
     * @param context Execution context
     * @return Execution context
     * @throws Exception Execution exception
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

    private Map<String, String> getHeaders(HttpRequestBase httpRequest) {
        Map<String, String> headerMap = new HashMap<>();
        for (Header header : httpRequest.getAllHeaders()) {
            headerMap.putIfAbsent(header.getName(), header.getValue());
        }
        return headerMap;
    }

    private void handleXdsRouterAndUpdateHttpRequest(Object[] arguments) {
        if (!routerConfig.isEnabledXdsRoute()) {
            return;
        }
        HttpRequestBase httpRequest = (HttpRequestBase) arguments[1];
        URI uri = httpRequest.getURI();
        String host = uri.getHost();
        if (!BaseHttpRouterUtils.isXdsRouteRequired(host)) {
            return;
        }

        // use xds route to find a service instance, and modify url by it
        Optional<ServiceInstance> serviceInstanceOptional = BaseHttpRouterUtils
                .chooseServiceInstanceByXds(host.split("\\.")[0], uri.getPath(), getHeaders(httpRequest));
        if (!serviceInstanceOptional.isPresent()) {
            return;
        }
        ServiceInstance instance = serviceInstanceOptional.get();
        try {
            httpRequest.setURI(new URI(BaseHttpRouterUtils.rebuildUrlByXdsServiceInstance(uri, instance)));
            arguments[0] = new HttpHost(instance.getHost(), instance.getPort());
        } catch (URISyntaxException e) {
            LOGGER.log(Level.WARNING, "Create uri using xds service instance failed.", e.getMessage());
        }
    }
}
