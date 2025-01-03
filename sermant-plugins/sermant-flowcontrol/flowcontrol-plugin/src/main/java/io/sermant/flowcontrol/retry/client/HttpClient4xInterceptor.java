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

package io.sermant.flowcontrol.retry.client;

import io.sermant.core.common.LoggerFactory;
import io.sermant.core.plugin.agent.entity.ExecuteContext;
import io.sermant.core.service.xds.entity.ServiceInstance;
import io.sermant.flowcontrol.AbstractXdsHttpClientInterceptor;
import io.sermant.flowcontrol.common.config.CommonConst;
import io.sermant.flowcontrol.common.entity.FlowControlResult;
import io.sermant.flowcontrol.common.entity.FlowControlScenario;
import io.sermant.flowcontrol.common.entity.HttpRequestEntity;
import io.sermant.flowcontrol.common.entity.RequestEntity;
import io.sermant.flowcontrol.common.handler.retry.AbstractRetry;
import io.sermant.flowcontrol.common.util.XdsRouterUtils;
import io.sermant.flowcontrol.common.util.XdsThreadLocalUtil;
import io.sermant.flowcontrol.inject.ErrorCloseableHttpResponse;

import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.util.EntityUtils;

import java.io.Closeable;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * HTTP interception only for version 4. x
 *
 * @author zhp
 * @since 2024-12-20
 */
public class HttpClient4xInterceptor extends AbstractXdsHttpClientInterceptor {
    private static final Logger LOGGER = LoggerFactory.getLogger();

    /**
     * Constructor
     */
    public HttpClient4xInterceptor() {
        super(new HttpClientRetry(), HttpClient4xInterceptor.class.getName());
    }

    /**
     * Pre-trigger point
     *
     * @param context Execution context
     * @return Execution context
     */
    @Override
    public ExecuteContext doBefore(ExecuteContext context) {
        Object[] arguments = context.getArguments();
        Object httpRequestObject = arguments[1];
        if (!(httpRequestObject instanceof HttpRequestBase)) {
            return context;
        }
        final HttpRequestBase httpRequest = (HttpRequestBase) httpRequestObject;

        // Parse the service name, request path, and request header in the request information and convert them into
        // a request entity class
        final Optional<HttpRequestEntity> httpRequestEntity = convertToValidHttpEntity(httpRequest);
        if (!httpRequestEntity.isPresent()) {
            return context;
        }
        final FlowControlResult flowControlResult = new FlowControlResult();

        // Execute the flow control handler chain, with only fault for XDS
        chooseHttpService().onBefore(className, httpRequestEntity.get(), flowControlResult);

        // When triggering some flow control rules, it is necessary to skip execution and return the result directly
        if (flowControlResult.isSkip()) {
            context.skip(new ErrorCloseableHttpResponse(flowControlResult.getResponse().getCode(),
                    flowControlResult.buildResponseMsg(), httpRequest.getProtocolVersion()));
            return context;
        }

        // Determine whether the number of requests has reached the threshold, and trigger flow control when the
        // threshold is reached
        if (isNeedCircuitBreak()) {
            context.skip(new ErrorCloseableHttpResponse(HttpStatus.SC_INTERNAL_SERVER_ERROR, MESSAGE,
                    httpRequest.getProtocolVersion()));
            return context;
        }

        // Execute service invocation and retry logic
        executeWithRetryPolicy(context);
        return context;
    }

    private Optional<HttpRequestEntity> convertToValidHttpEntity(HttpRequestBase httpRequest) {
        if (httpRequest == null || httpRequest.getURI() == null) {
            return Optional.empty();
        }
        URI uri = httpRequest.getURI();
        String host = uri.getHost();
        String serviceName = host.split(CommonConst.ESCAPED_POINT)[0];
        if (!XdsRouterUtils.isXdsRouteRequired(serviceName)) {
            return Optional.empty();
        }
        final Map<String, String> headers = getHeaders(httpRequest);
        return Optional.of(new HttpRequestEntity.Builder()
                .setRequestType(RequestEntity.RequestType.CLIENT)
                .setApiPath(uri.getPath()).setHeaders(headers)
                .setMethod(httpRequest.getMethod())
                .setServiceName(serviceName)
                .build());
    }

    private Map<String, String> getHeaders(HttpRequest httpRequest) {
        Map<String, String> headerMap = new HashMap<>();
        for (Header header : httpRequest.getAllHeaders()) {
            headerMap.putIfAbsent(header.getName(), header.getValue());
        }
        return headerMap;
    }

    @Override
    public int getStatusCode(ExecuteContext context) {
        Optional<String> statusCodeOptional = retry.getCode(context.getResult());
        return statusCodeOptional.map(Integer::parseInt).orElse(CommonConst.DEFAULT_RESPONSE_CODE);
    }

    @Override
    protected void preRetry(Object obj, Method method, Object[] allArguments, Object result, boolean isFirstInvoke) {
        tryClose(result);
        Optional<ServiceInstance> serviceInstanceOptional = chooseServiceInstanceForXds();
        if (!serviceInstanceOptional.isPresent()) {
            return;
        }
        ServiceInstance instance = serviceInstanceOptional.get();
        FlowControlScenario scenarioInfo = XdsThreadLocalUtil.getScenarioInfo();
        scenarioInfo.setAddress(instance.getHost() + CommonConst.CONNECT + instance.getPort());
        final HttpRequestBase httpRequest = (HttpRequestBase) allArguments[1];
        try {
            httpRequest.setURI(new URI(XdsRouterUtils.rebuildUrlByXdsServiceInstance(httpRequest.getURI(), instance)));
            allArguments[0] = new HttpHost(instance.getHost(), instance.getPort());
        } catch (URISyntaxException e) {
            LOGGER.log(Level.WARNING, "Create uri using xds service instance failed.", e.getMessage());
        }
    }

    private void tryClose(Object result) {
        if (!(result instanceof HttpResponse)) {
            return;
        }
        HttpResponse httpResponse = (HttpResponse) result;
        try {
            try {
                EntityUtils.consume(httpResponse.getEntity());
            } finally {
                if (httpResponse instanceof Closeable) {
                    ((Closeable) httpResponse).close();
                }
            }
        } catch (IOException ex) {
            LOGGER.severe("An exception occurred when attempting to close the httpResponse.");
        }
    }

    /**
     * Http Client retry
     *
     * @since 2022-02-21
     */
    public static class HttpClientRetry extends AbstractRetry {
        @Override
        public Optional<String> getCode(Object result) {
            if (!(result instanceof CloseableHttpResponse)) {
                return Optional.empty();
            }
            CloseableHttpResponse httpResponse = (CloseableHttpResponse) result;
            if (httpResponse.getStatusLine() == null) {
                return Optional.empty();
            }
            return Optional.of(String.valueOf(httpResponse.getStatusLine().getStatusCode()));
        }

        @Override
        public Optional<Set<String>> getHeaderNames(Object result) {
            if (!(result instanceof CloseableHttpResponse)) {
                return Optional.empty();
            }
            CloseableHttpResponse httpResponse = (CloseableHttpResponse) result;
            Set<String> headerNames = new HashSet<>();
            for (Header header : httpResponse.getAllHeaders()) {
                headerNames.add(header.getName());
            }
            return Optional.of(headerNames);
        }

        @Override
        public Class<? extends Throwable>[] retryExceptions() {
            return getRetryExceptions();
        }

        @Override
        public RetryFramework retryType() {
            return RetryFramework.SPRING;
        }
    }
}
