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
import io.sermant.router.common.constants.RouterConstant;
import io.sermant.router.common.request.RequestData;
import io.sermant.router.common.utils.FlowContextUtils;
import io.sermant.router.common.utils.ThreadLocalUtils;
import io.sermant.router.spring.entity.HttpAsyncRequestProducerDecorator;
import io.sermant.router.spring.utils.BaseHttpRouterUtils;

import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.nio.protocol.HttpAsyncRequestProducer;
import org.apache.http.protocol.HttpContext;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * HTTP interception only for version 4. x
 *
 * @author yangrh
 * @since 2022-10-31
 */
public class HttpAsyncClient4xInterceptor extends MarkInterceptor {
    private static final Logger LOGGER = LoggerFactory.getLogger();

    private static final int HTTPCONTEXT_INDEX = 2;

    private RouterConfig routerConfig = PluginConfigManager.getPluginConfig(RouterConfig.class);

    /**
     * Pre trigger point
     *
     * @param context Execution context
     * @return Execution context
     * @throws Exception Execution exception
     */
    @Override
    public ExecuteContext doBefore(ExecuteContext context) throws Exception {
        LogUtils.printHttpRequestBeforePoint(context);
        Object httpAsyncRequestProducerArgument = context.getArguments()[0];
        if (!(httpAsyncRequestProducerArgument instanceof HttpAsyncRequestProducer)) {
            return context;
        }
        HttpAsyncRequestProducer httpAsyncRequestProducer
                = (HttpAsyncRequestProducer) httpAsyncRequestProducerArgument;
        HttpRequest httpRequest = httpAsyncRequestProducer.generateRequest();
        handleXdsRouterAndUpdateHttpRequest(httpRequest, context);
        Object argument = context.getArguments()[HTTPCONTEXT_INDEX];
        if (!(argument instanceof HttpContext)) {
            return context;
        }
        HttpContext httpContext = (HttpContext) argument;
        if (StringUtils.isBlank(FlowContextUtils.getTagName())) {
            return context;
        }
        parseTags(httpContext, httpRequest);
        return context;
    }

    private void parseTags(HttpContext httpContext, HttpRequest httpRequest) {
        Object attribute = httpContext.getAttribute(FlowContextUtils.getTagName());
        if (attribute != null) {
            Map<String, List<String>> map = FlowContextUtils.decodeTags(String.valueOf(attribute));
            if (map != null && map.size() > 0) {
                ThreadLocalUtils.setRequestData(new RequestData(
                        map, httpRequest.getRequestLine().getUri(), httpRequest.getRequestLine().getMethod()));
            }
        }
    }

    /**
     * Post-Interception Point Note: This method does not remove thread variables from the post-interception point, but
     * obtains thread variables at the NopInstanceFilterInterceptor pre-interceptor point for traffic routing， Remove
     * thread variables from the NopInstanceFilterInterceptor interceptor end. Note: httpasyncclient uses the
     * future.get() logic that must have a synchronous thread, otherwise the thread variable cannot be removed
     *
     * @param context Execution context
     * @return Execution context
     * @throws Exception Execution exception
     */
    @Override
    public ExecuteContext after(ExecuteContext context) throws Exception {
        LogUtils.printHttpRequestAfterPoint(context);
        return context;
    }

    @Override
    public ExecuteContext onThrow(ExecuteContext context) {
        ThreadLocalUtils.removeRequestData();
        LogUtils.printHttpRequestOnThrowPoint(context);
        return context;
    }

    private Map<String, String> getHeaders(HttpRequest httpRequest) {
        Map<String, String> headerMap = new HashMap<>();
        for (Header header : httpRequest.getAllHeaders()) {
            headerMap.putIfAbsent(header.getName(), header.getValue());
        }
        return headerMap;
    }

    private void handleXdsRouterAndUpdateHttpRequest(HttpRequest httpRequest, ExecuteContext context) {
        if (!routerConfig.isEnabledXdsRoute()) {
            return;
        }
        URI uri = URI.create(httpRequest.getRequestLine().getUri());
        String host = uri.getHost();
        if (!BaseHttpRouterUtils.isXdsRouteRequired(host)) {
            return;
        }

        // use xds route to find a service instance, and modify url by it
        Optional<ServiceInstance> serviceInstanceOptional = BaseHttpRouterUtils
                .chooseServiceInstanceByXds(host.split(RouterConstant.ESCAPED_POINT)[0], uri.getPath(),
                        getHeaders(httpRequest));
        if (!serviceInstanceOptional.isPresent()) {
            return;
        }
        ServiceInstance instance = serviceInstanceOptional.get();
        try {
            context.getArguments()[0] = rebuildProducer(context,
                    new URI(BaseHttpRouterUtils.rebuildUrlByXdsServiceInstance(uri, instance)));
        } catch (URISyntaxException e) {
            LOGGER.log(Level.WARNING, "Create uri using xds service instance failed.", e.getMessage());
        }
    }

    private HttpAsyncRequestProducer rebuildProducer(ExecuteContext context, URI newUri) {
        return new HttpAsyncRequestProducerDecorator((HttpAsyncRequestProducer) context.getArguments()[0],
                buildRequestDecorator(newUri), buildHostDecorator(newUri));
    }

    private Function<HttpHost, HttpHost> buildHostDecorator(URI newUri) {
        return httpHost -> rebuildHttpHost(newUri);
    }

    private Function<HttpRequest, HttpRequest> buildRequestDecorator(URI newUri) {
        return httpRequest -> updateRequestUri(newUri, httpRequest);
    }

    private HttpHost rebuildHttpHost(URI newUri) {
        return URIUtils.extractHost(newUri);
    }

    private HttpRequest updateRequestUri(URI newUri, HttpRequest httpUriRequest) {
        HttpRequestBase httpRequest = (HttpRequestBase) httpUriRequest;
        httpRequest.setURI(newUri);
        return httpRequest;
    }
}
