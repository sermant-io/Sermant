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
import io.sermant.core.utils.CollectionUtils;
import io.sermant.core.utils.MapUtils;
import io.sermant.core.utils.ReflectUtils;
import io.sermant.flowcontrol.AbstractXdsHttpClientInterceptor;
import io.sermant.flowcontrol.common.config.CommonConst;
import io.sermant.flowcontrol.common.entity.FlowControlResult;
import io.sermant.flowcontrol.common.entity.FlowControlScenario;
import io.sermant.flowcontrol.common.entity.HttpRequestEntity;
import io.sermant.flowcontrol.common.entity.RequestEntity;
import io.sermant.flowcontrol.common.util.XdsRouterUtils;
import io.sermant.flowcontrol.common.util.XdsThreadLocalUtil;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * An enhanced interceptor for java.net.HttpURLConnection in JDK version 1.8<br>
 *
 * @author yuzl Yu Zhenlong
 * @since 2024-12-20
 */
public class HttpUrlConnectionConnectInterceptor extends AbstractXdsHttpClientInterceptor {
    private static final Logger LOGGER = LoggerFactory.getLogger();

    /**
     * Constructor
     */
    public HttpUrlConnectionConnectInterceptor() {
        super(new HttpUrlConnectionResponseStreamInterceptor.HttpUrlConnectionRetry(),
                HttpUrlConnectionConnectInterceptor.class.getName());
    }

    @Override
    public ExecuteContext doBefore(ExecuteContext context) {
        if (!(context.getObject() instanceof HttpURLConnection)) {
            return context;
        }
        HttpURLConnection connection = (HttpURLConnection) context.getObject();

        // Parse the service name, request path, and request header in the request information and convert them into
        // a request entity class
        final Optional<HttpRequestEntity> httpRequestEntity = convertToValidHttpEntity(connection);
        if (!httpRequestEntity.isPresent()) {
            return context;
        }
        final FlowControlResult flowControlResult = new FlowControlResult();

        // Execute the flow control handler chain, with only fault for XDS
        chooseHttpService().onBefore(className, httpRequestEntity.get(), flowControlResult);

        // When triggering some flow control rules, it is necessary to skip execution and return the result directly
        if (flowControlResult.isSkip()) {
            context.setThrowableOut(new RuntimeException(flowControlResult.buildResponseMsg()));
            setResponseCodeAndMessage(connection, flowControlResult.getResponse().getCode(),
                    flowControlResult.buildResponseMsg());
            context.skip(null);
            return context;
        }

        // Determine whether the number of requests has reached the threshold, and trigger flow control when the
        // threshold is reached
        if (isNeedCircuitBreak()) {
            context.setThrowableOut(new RuntimeException(MESSAGE));
            setResponseCodeAndMessage(connection, CommonConst.INTERVAL_SERVER_ERROR, MESSAGE);
            context.skip(null);
            return context;
        }

        // Execute service invocation and retry logic
        executeWithRetryPolicy(context);
        return context;
    }

    private static void setResponseCodeAndMessage(HttpURLConnection connection, int code, String message) {
        ReflectUtils.setFieldValue(connection, "responseCode", code);
        ReflectUtils.setFieldValue(connection, "responseMessage", message);
    }

    @Override
    public ExecuteContext doAfter(ExecuteContext context) {
        return context;
    }

    @Override
    public ExecuteContext doThrow(ExecuteContext context) {
        return context;
    }

    private Optional<HttpRequestEntity> convertToValidHttpEntity(HttpURLConnection connection) {
        if (connection == null || connection.getURL() == null) {
            return Optional.empty();
        }
        URL uri = connection.getURL();
        String host = uri.getHost();
        String serviceName = host.split(CommonConst.ESCAPED_POINT)[0];
        if (!XdsRouterUtils.isXdsRouteRequired(serviceName)) {
            return Optional.empty();
        }
        final Map<String, String> headers = getHeaders(connection);
        return Optional.of(new HttpRequestEntity.Builder()
                .setRequestType(RequestEntity.RequestType.CLIENT)
                .setApiPath(uri.getPath()).setHeaders(headers)
                .setMethod(connection.getRequestMethod())
                .setServiceName(serviceName)
                .build());
    }

    private Map<String, String> getHeaders(HttpURLConnection connection) {
        Map<String, String> headerMap = new HashMap<>();
        if (MapUtils.isEmpty(connection.getRequestProperties())) {
            return headerMap;
        }
        for (Map.Entry<String, List<String>> header : connection.getRequestProperties().entrySet()) {
            if (CollectionUtils.isEmpty(header.getValue())) {
                continue;
            }
            headerMap.putIfAbsent(header.getKey(), header.getValue().get(0));
        }
        return headerMap;
    }

    @Override
    public int getStatusCode(ExecuteContext context) {
        HttpURLConnection connection = (HttpURLConnection) context.getObject();
        try {
            return connection.getResponseCode();
        } catch (IOException io) {
            LOGGER.log(Level.SEVERE, "Failed to get response code.", io);
            return CommonConst.DEFAULT_RESPONSE_CODE;
        }
    }

    @Override
    protected void preRetry(Object obj, Method method, Object[] allArguments, Object result, boolean isFirstInvoke) {
        Optional<ServiceInstance> serviceInstanceOptional = chooseServiceInstanceForXds();
        if (!serviceInstanceOptional.isPresent()) {
            return;
        }
        HttpURLConnection connection = (HttpURLConnection) obj;
        ServiceInstance instance = serviceInstanceOptional.get();
        FlowControlScenario scenarioInfo = XdsThreadLocalUtil.getScenarioInfo();
        scenarioInfo.setAddress(instance.getHost() + CommonConst.CONNECT + instance.getPort());
        try {
            URL url = connection.getURL();
            URL newUrl = new URL(url.getProtocol(), instance.getHost(), instance.getPort(), url.getFile());
            ReflectUtils.setFieldValue(connection, "url", newUrl);
            tryResetProxy(newUrl, obj);
        } catch (MalformedURLException e) {
            LOGGER.log(Level.WARNING, "Create url using xds service instance failed.", e.getMessage());
        }
    }

    /**
     * In the scenario of specifying a proxy, you need to replace the address of the proxy with the actual downstream
     * address; otherwise 404 will appear
     *
     * @param newUrl Actual downstream address
     * @param object Enhanced object
     */
    private void tryResetProxy(URL newUrl, Object object) {
        final Optional<Object> instProxy = ReflectUtils.getFieldValue(object, "instProxy");
        if (!instProxy.isPresent() || !(instProxy.get() instanceof Proxy)) {
            return;
        }
        final Proxy proxy = (Proxy) instProxy.get();
        if (proxy.type() != Proxy.Type.HTTP) {
            return;
        }

        // The user uses its own proxy to replace the resolved downstream address
        ReflectUtils.setFieldValue(object, "instProxy", getProxy(newUrl));
    }

    private Proxy getProxy(URL newUrl) {
        return createProxy(newUrl);
    }

    private Proxy createProxy(URL newUrl) {
        return new Proxy(Proxy.Type.HTTP, InetSocketAddress.createUnresolved(newUrl.getHost(), newUrl.getPort()));
    }
}
