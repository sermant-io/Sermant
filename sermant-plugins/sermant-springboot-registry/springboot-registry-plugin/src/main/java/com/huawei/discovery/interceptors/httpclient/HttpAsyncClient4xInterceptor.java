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

package com.huawei.discovery.interceptors.httpclient;

import com.huawei.discovery.entity.FutureDecorator;
import com.huawei.discovery.entity.HttpAsyncContext;
import com.huawei.discovery.entity.HttpAsyncInvokerResult;
import com.huawei.discovery.entity.ServiceInstance;
import com.huawei.discovery.retry.InvokerContext;
import com.huawei.discovery.service.InvokerService;
import com.huawei.discovery.utils.HttpAsyncUtils;
import com.huawei.discovery.utils.HttpConstants;
import com.huawei.discovery.utils.PlugEffectWhiteBlackUtils;
import com.huawei.discovery.utils.RequestInterceptorUtils;

import com.huaweicloud.sermant.core.plugin.agent.entity.ExecuteContext;
import com.huaweicloud.sermant.core.plugin.agent.interceptor.Interceptor;
import com.huaweicloud.sermant.core.plugin.service.PluginServiceManager;
import com.huaweicloud.sermant.core.utils.ClassUtils;
import com.huaweicloud.sermant.core.utils.ReflectUtils;

import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.ProtocolVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.EnglishReasonPhraseCatalog;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.message.BasicStatusLine;
import org.apache.http.nio.protocol.HttpAsyncRequestProducer;

import java.net.URI;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * 仅针对4.x版本得http拦截
 *
 * @author zhouss
 * @since 2022-10-10
 */
public class HttpAsyncClient4xInterceptor implements Interceptor {
    private static final String COMMON_REQUEST_CLASS = "com.huawei.discovery.entity.HttpCommonRequest";

    private static final String PRODUCER_CLASS = "com.huawei.discovery.entity.HttpAsyncRequestProducerDecorator";

    private static final String FUTURE_CLASS = "com.huawei.discovery.entity.FutureDecorator";

    private final AtomicBoolean isLoaded = new AtomicBoolean();

    @Override
    public ExecuteContext before(ExecuteContext context) throws Exception {
        ready();
        HttpAsyncRequestProducer httpAsyncRequestProducer = (HttpAsyncRequestProducer) context.getArguments()[0];
        if (!PlugEffectWhiteBlackUtils.isHostEqualRealmName(httpAsyncRequestProducer.getTarget().getHostName())) {
            return context;
        }
        acquireHostPath(httpAsyncRequestProducer);
        if (!isConfigEnable()) {
            // 配置不允许则直接返回
            return context;
        }
        final ServiceInstance selectedInstance = HttpAsyncUtils.getOrCreateContext().getSelectedInstance();
        if (selectedInstance == null) {
            // 置空回调, 阻止第一次因url问题导致回调给与用户错误结果
            context.skip(null);
            HttpAsyncUtils.getOrCreateContext().setCallback(context.getArguments()[HttpAsyncContext.CALL_BACK_INDEX]);
            context.getArguments()[HttpAsyncContext.CALL_BACK_INDEX] = null;
            return context;
        }
        return context;
    }

    @Override
    public ExecuteContext after(ExecuteContext context) throws Exception {
        if (!isConfigEnable()) {
            HttpAsyncUtils.remove();
            return context;
        }
        final ClassLoader appClassloader = Thread.currentThread().getContextClassLoader();
        try {
            if (HttpAsyncUtils.getOrCreateContext().getSelectedInstance() != null) {
                return context;
            }
            RequestInterceptorUtils.printRequestLog("HttpAsyncClient", HttpAsyncUtils.getOrCreateContext()
                    .getHostAndPath());
            final Map<String, String> hostAndPath = HttpAsyncUtils.getOrCreateContext().getHostAndPath();
            if (hostAndPath == null) {
                return context;
            }
            final String serviceName = hostAndPath.get(HttpConstants.HTTP_URI_SERVICE);
            cleanCallback();
            final InvokerService invokerService = PluginServiceManager.getPluginService(InvokerService.class);
            final HttpAsyncContext asyncContext = HttpAsyncUtils.getOrCreateContext();
            Thread.currentThread().setContextClassLoader(HttpClient.class.getClassLoader());

            // 对future进行修饰, 增加异常重试逻辑
            context.changeResult(new FutureDecorator(buildInvokerBiFunc(asyncContext, invokerService, serviceName,
                    context)));
            return context;
        } finally {
            HttpAsyncUtils.remove();
            Thread.currentThread().setContextClassLoader(appClassloader);
        }
    }

    private boolean isConfigEnable() {
        final String originHostName = HttpAsyncUtils.getOrCreateContext().getOriginHostName();
        final Map<String, String> hostAndPath = HttpAsyncUtils.getOrCreateContext().getHostAndPath();
        if (originHostName == null || hostAndPath == null) {
            return false;
        }
        return PlugEffectWhiteBlackUtils.isAllowRun(originHostName, hostAndPath.get(HttpConstants.HTTP_URI_SERVICE));
    }

    private void acquireHostPath(HttpAsyncRequestProducer httpAsyncRequestProducer) throws Exception {
        final HttpHost httpHost = httpAsyncRequestProducer.getTarget();
        final HttpRequest httpRequest = httpAsyncRequestProducer.generateRequest();
        final Optional<URI> optionalUri = RequestInterceptorUtils.formatUri(httpRequest.getRequestLine().getUri());
        if (!optionalUri.isPresent()) {
            return;
        }
        URI uri = optionalUri.get();
        Map<String, String> hostAndPath = RequestInterceptorUtils.recoverHostAndPath(uri.getPath());
        HttpAsyncUtils.getOrCreateContext().setHostAndPath(hostAndPath);
        HttpAsyncUtils.getOrCreateContext().setUri(uri);
        HttpAsyncUtils.getOrCreateContext().setMethod(httpRequest.getRequestLine().getMethod());
        HttpAsyncUtils.getOrCreateContext().setOriginHostName(httpHost.getHostName());
    }

    private HttpAsyncRequestProducer rebuildProducer(ExecuteContext context, ServiceInstance selectedInstance) {
        HttpAsyncRequestProducer httpAsyncRequestProducer = (HttpAsyncRequestProducer) context.getArguments()[0];
        final HttpAsyncContext asyncContext = HttpAsyncUtils.getOrCreateContext();
        return rebuildProducer(httpAsyncRequestProducer, asyncContext.getUri(),
                asyncContext.getMethod(),
                selectedInstance,
                asyncContext.getHostAndPath());
    }

    private HttpAsyncRequestProducer rebuildProducer(HttpAsyncRequestProducer producer, URI requestPath, String method,
            ServiceInstance serviceInstance, Map<String, String> hostAndPath) {
        String uriNew = RequestInterceptorUtils.buildUrlWithIp(requestPath, serviceInstance,
                hostAndPath.get(HttpConstants.HTTP_URI_PATH), method);
        final Optional<Object> result = ReflectUtils
                .buildWithConstructor("com.huawei.discovery.entity.HttpAsyncRequestProducerDecorator",
                        new Class[]{HttpAsyncRequestProducer.class, Function.class, Function.class},
                        new Object[]{producer, buildRequestDecorator(uriNew, method),
                                buildHostDecorator(uriNew)});
        return result.map(o -> (HttpAsyncRequestProducer) o).orElse(producer);
    }

    private BiFunction<Long, TimeUnit, HttpAsyncInvokerResult> buildInvokerBiFunc(HttpAsyncContext asyncContext,
            InvokerService invokerService, String serviceName, ExecuteContext context) {
        return (timeout, timeUnit) -> {
            try {
                final Optional<Object> invokerResult = invokerService
                        .invoke(buildInvokerFunc(timeout, timeUnit, context, asyncContext), ex -> ex, serviceName);
                if (invokerResult.isPresent()) {
                    final HttpAsyncInvokerResult result = formatResult(invokerResult.get());
                    notify(asyncContext, result.getResult());
                    return result;
                }

                // 该场景仅当无实例才会触发, 此处模拟未拦截触发的异常, 即404
                final HttpAsyncInvokerResult result = mockErrorResult();
                notify(asyncContext, result.getResult());
                return result;
            } finally {
                HttpAsyncUtils.remove();
            }
        };
    }

    private HttpAsyncInvokerResult mockErrorResult() {
        String msg = "Not Found";
        final BasicHttpResponse basicHttpResponse = new BasicHttpResponse(new BasicStatusLine(
                new ProtocolVersion("HTTP", 1, 1), HttpStatus.SC_NOT_FOUND, msg),
                EnglishReasonPhraseCatalog.INSTANCE, Locale.SIMPLIFIED_CHINESE);
        basicHttpResponse.setEntity(new StringEntity(msg, ContentType.TEXT_HTML));
        return new HttpAsyncInvokerResult(null, basicHttpResponse);
    }

    private HttpAsyncInvokerResult formatResult(Object result) {
        if (result instanceof HttpAsyncInvokerResult) {
            return (HttpAsyncInvokerResult) result;
        }
        return new HttpAsyncInvokerResult(null, result);
    }

    private void notify(HttpAsyncContext asyncContext, Object response) {
        final Object callback = asyncContext.getCallback();
        if (callback == null) {
            return;
        }
        FutureCallback<HttpResponse> cur = (FutureCallback<HttpResponse>) callback;
        if (response instanceof Throwable) {
            cur.failed((Exception) response);
        } else {
            cur.completed((HttpResponse) response);
        }
    }

    private void cleanCallback() {
        final Object handler = HttpAsyncUtils.getOrCreateContext().getHandler();
        final Optional<Object> resultFuture = ReflectUtils.getFieldValue(handler, "resultFuture");
        if (!resultFuture.isPresent()) {
            return;
        }
        ReflectUtils.setFieldValue(resultFuture.get(), "callback", null);
    }

    @Override
    public ExecuteContext onThrow(ExecuteContext context) {
        return context;
    }

    private Function<InvokerContext, Object> buildInvokerFunc(long timeout, TimeUnit timeUnit, ExecuteContext context,
            HttpAsyncContext asyncContext) {
        return invokerContext -> {
            Object finalResult;
            Object finalFuture = null;
            try {
                resetResponseConsumer(context);
                copyToCurThread(asyncContext, invokerContext.getServiceInstance());
                context.getArguments()[0] = rebuildProducer(context, invokerContext.getServiceInstance());
                final Supplier<Object> supplier = RequestInterceptorUtils.buildFunc(context, invokerContext);
                finalFuture = supplier.get();
                try {
                    finalResult = futureInvoke((Future<HttpResponse>) finalFuture, timeout, timeUnit);
                } catch (ExecutionException ex) {
                    invokerContext.setEx(ex.getCause());
                    finalResult = ex;
                } catch (InterruptedException | TimeoutException ex) {
                    invokerContext.setEx(ex);
                    finalResult = ex;
                }
            } catch (Exception exception) {
                invokerContext.setEx(exception);
                finalResult = exception;
            }
            return new HttpAsyncInvokerResult(finalFuture, finalResult);
        };
    }

    private void copyToCurThread(HttpAsyncContext asyncContext, ServiceInstance serviceInstance) {
        HttpAsyncUtils.getOrCreateContext().setCallback(asyncContext.getCallback());
        HttpAsyncUtils.getOrCreateContext().setHandler(asyncContext.getHandler());
        HttpAsyncUtils.getOrCreateContext().setSelectedInstance(serviceInstance);
        HttpAsyncUtils.getOrCreateContext().setHostAndPath(asyncContext.getHostAndPath());
        HttpAsyncUtils.getOrCreateContext().setUri(asyncContext.getUri());
        HttpAsyncUtils.getOrCreateContext().setMethod(asyncContext.getMethod());
        HttpAsyncUtils.getOrCreateContext().setOriginHostName(asyncContext.getOriginHostName());
    }

    private void resetResponseConsumer(ExecuteContext context) {
        final Object responseConsumer = context.getArguments()[1];
        final Optional<Object> completed = ReflectUtils.getFieldValue(responseConsumer, "completed");
        if (completed.isPresent() && completed.get() instanceof AtomicBoolean) {
            ((AtomicBoolean) completed.get()).set(false);
        }
        ReflectUtils.setFieldValue(responseConsumer, "result", null);
        ReflectUtils.setFieldValue(responseConsumer, "ex", null);
    }

    private HttpResponse futureInvoke(Future<HttpResponse> future, long timeout, TimeUnit timeUnit)
            throws ExecutionException, InterruptedException, TimeoutException {
        if (timeUnit == null) {
            return future.get();
        } else {
            return future.get(timeout, timeUnit);
        }
    }

    private Function<HttpHost, HttpHost> buildHostDecorator(String uriNew) {
        return httpHost -> rebuildHttpHost(uriNew);
    }

    private Function<HttpRequest, HttpRequest> buildRequestDecorator(String uriNew, String method) {
        return httpRequest -> rebuildRequest(uriNew, method, httpRequest);
    }

    private void ready() {
        if (isLoaded.compareAndSet(false, true)) {
            final ClassLoader classLoader = HttpClient.class.getClassLoader();
            ClassUtils.defineClass(PRODUCER_CLASS, classLoader);
            ClassUtils.defineClass(FUTURE_CLASS, classLoader);
            ClassUtils.defineClass(COMMON_REQUEST_CLASS, classLoader);
        }
    }

    private HttpHost rebuildHttpHost(String uriNew) {
        final Optional<URI> optionalUri = RequestInterceptorUtils.formatUri(uriNew);
        if (optionalUri.isPresent()) {
            return URIUtils.extractHost(optionalUri.get());
        }
        throw new IllegalArgumentException("Invalid url: " + uriNew);
    }

    private HttpRequest rebuildRequest(String uriNew, String method, HttpRequest httpUriRequest) {
        if (httpUriRequest instanceof HttpPost) {
            HttpPost oldHttpPost = (HttpPost) httpUriRequest;
            HttpPost httpPost = new HttpPost(uriNew);
            httpPost.setEntity(oldHttpPost.getEntity());
            return httpPost;
        } else {
            final ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
            Thread.currentThread().setContextClassLoader(HttpClient.class.getClassLoader());
            final Optional<Object> result = ReflectUtils
                    .buildWithConstructor(COMMON_REQUEST_CLASS,
                            new Class[]{String.class, String.class}, new Object[]{method, uriNew});
            Thread.currentThread().setContextClassLoader(contextClassLoader);
            return (HttpRequest) result.orElse(null);
        }
    }
}
