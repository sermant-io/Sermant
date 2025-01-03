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

import io.sermant.core.plugin.agent.entity.ExecuteContext;
import io.sermant.core.service.xds.entity.ServiceInstance;
import io.sermant.core.utils.LogUtils;
import io.sermant.core.utils.ReflectUtils;
import io.sermant.flowcontrol.AbstractXdsHttpClientInterceptor;
import io.sermant.flowcontrol.common.config.CommonConst;
import io.sermant.flowcontrol.common.entity.FlowControlResult;
import io.sermant.flowcontrol.common.entity.FlowControlScenario;
import io.sermant.flowcontrol.common.entity.HttpRequestEntity;
import io.sermant.flowcontrol.common.entity.RequestEntity;
import io.sermant.flowcontrol.common.exception.InvokerWrapperException;
import io.sermant.flowcontrol.common.handler.retry.AbstractRetry;
import io.sermant.flowcontrol.common.util.XdsRouterUtils;
import io.sermant.flowcontrol.common.util.XdsThreadLocalUtil;
import okhttp3.Headers;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

/**
 * Blocking for okHttp3.x and above versions
 *
 * @author zhp
 * @since 2024-12-20
 */
public class OkHttp3ClientInterceptor extends AbstractXdsHttpClientInterceptor {
    private static final String REQUEST_FIELD_NAME = "originalRequest";

    private final String className = HttpClient4xInterceptor.class.getName();

    /**
     * Constructor
     */
    public OkHttp3ClientInterceptor() {
        super(new OkHttp3Retry(), OkHttp3ClientInterceptor.class.getCanonicalName());
    }

    @Override
    protected ExecuteContext doBefore(ExecuteContext context) throws Exception {
        LogUtils.printHttpRequestBeforePoint(context);
        final Optional<Request> rawRequest = getRequest(context.getObject());
        if (!rawRequest.isPresent()) {
            return context;
        }
        context.setLocalFieldValue(REQUEST_FIELD_NAME, rawRequest.get());
        Request request = rawRequest.get();

        // Parse the service name, request path, and request header in the request information and convert them into
        // a request entity class
        final Optional<HttpRequestEntity> httpRequestEntity = convertToValidHttpEntity(request);
        if (!httpRequestEntity.isPresent()) {
            return context;
        }
        final FlowControlResult flowControlResult = new FlowControlResult();

        // Execute the flow control handler chain, with only fault for XDS
        chooseHttpService().onBefore(className, httpRequestEntity.get(), flowControlResult);

        // When triggering some flow control rules, it is necessary to skip execution and return the result directly
        if (flowControlResult.isSkip()) {
            Response.Builder builder = new Response.Builder();
            context.skip(builder.code(flowControlResult.getResponse().getCode())
                    .protocol(Protocol.HTTP_1_1)
                    .message(flowControlResult.buildResponseMsg()).request(request).build());
            return context;
        }

        // Determine whether the number of requests has reached the threshold, and trigger flow control when the
        // threshold is reached
        if (isNeedCircuitBreak()) {
            Response.Builder builder = new Response.Builder();
            context.skip(builder.code(CommonConst.INTERVAL_SERVER_ERROR)
                    .message(MESSAGE).request(request).protocol(Protocol.HTTP_1_1).build());
            return context;
        }

        // Execute service invocation and retry logic
        executeWithRetryPolicy(context);
        return context;
    }

    private Optional<Request> getRequest(Object object) {
        final Optional<Object> originalRequest = ReflectUtils.getFieldValue(object, REQUEST_FIELD_NAME);
        if (originalRequest.isPresent() && originalRequest.get() instanceof Request) {
            return Optional.of((Request) originalRequest.get());
        }
        return Optional.empty();
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

    private Optional<HttpRequestEntity> convertToValidHttpEntity(Request httpRequest) {
        HttpUrl uri = httpRequest.url();
        String serviceName = uri.host().split(CommonConst.ESCAPED_POINT)[0];
        if (!XdsRouterUtils.isXdsRouteRequired(serviceName)) {
            return Optional.empty();
        }
        final Map<String, String> headers = getHeaders(httpRequest);
        return Optional.of(new HttpRequestEntity.Builder()
                .setRequestType(RequestEntity.RequestType.CLIENT)
                .setApiPath(uri.encodedPath()).setHeaders(headers)
                .setMethod(httpRequest.method())
                .setServiceName(serviceName)
                .build());
    }

    private Map<String, String> getHeaders(Request httpRequest) {
        Map<String, String> headerMap = new HashMap<>();
        Headers headers = httpRequest.headers();
        for (String name : headers.names()) {
            headerMap.putIfAbsent(name, headers.get(name));
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
        Optional<ServiceInstance> serviceInstanceOptional = chooseServiceInstanceForXds();
        if (!serviceInstanceOptional.isPresent()) {
            return;
        }
        final Optional<Request> rawRequest = getRequest(obj);
        if (!rawRequest.isPresent()) {
            return;
        }
        ServiceInstance instance = serviceInstanceOptional.get();
        FlowControlScenario scenarioInfo = XdsThreadLocalUtil.getScenarioInfo();
        scenarioInfo.setAddress(instance.getHost() + CommonConst.CONNECT + instance.getPort());
        ReflectUtils.setFieldValue(obj, REQUEST_FIELD_NAME, rebuildRequest(rawRequest.get(), instance));
    }

    private Object copyNewCall(Object object, Request newRequest) {
        final Optional<Object> client = ReflectUtils.getFieldValue(object, "client");
        if (!client.isPresent()) {
            return object;
        }
        final OkHttpClient okHttpClient = (OkHttpClient) client.get();
        return okHttpClient.newCall(newRequest);
    }

    @Override
    public Supplier<Object> createRetryFunc(ExecuteContext context, Object result) {
        Object obj = context.getObject();
        Method method = context.getMethod();
        Object[] allArguments = context.getArguments();
        final AtomicBoolean isFirstInvoke = new AtomicBoolean(true);
        return () -> {
            method.setAccessible(true);
            try {
                Request request = (Request) context.getLocalFieldValue(REQUEST_FIELD_NAME);
                preRetry(obj, method, allArguments, result, isFirstInvoke.get());
                Object newCall = copyNewCall(obj, request);
                Object invokeResult = method.invoke(newCall, allArguments);
                isFirstInvoke.compareAndSet(true, false);
                return invokeResult;
            } catch (IllegalAccessException ignored) {
                // ignored
            } catch (InvocationTargetException ex) {
                throw new InvokerWrapperException(ex.getTargetException());
            }
            return result;
        };
    }

    /**
     * OkHttp3 retry
     *
     * @since 2022-02-21
     */
    public static class OkHttp3Retry extends AbstractRetry {
        @Override
        public Optional<String> getCode(Object result) {
            if (!(result instanceof Response)) {
                return Optional.empty();
            }
            Response httpResponse = (Response) result;
            return Optional.of(String.valueOf(httpResponse.code()));
        }

        @Override
        public Optional<Set<String>> getHeaderNames(Object result) {
            if (!(result instanceof Response)) {
                return Optional.empty();
            }
            Response httpResponse = (Response) result;
            return Optional.of(httpResponse.headers().names());
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
