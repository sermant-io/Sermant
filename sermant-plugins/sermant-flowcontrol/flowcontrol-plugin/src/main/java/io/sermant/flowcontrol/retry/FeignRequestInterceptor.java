/*
 * Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package io.sermant.flowcontrol.retry;

import feign.Request;
import feign.Response;
import io.sermant.core.common.LoggerFactory;
import io.sermant.core.plugin.agent.entity.ExecuteContext;
import io.sermant.core.utils.StringUtils;
import io.sermant.flowcontrol.common.config.ConfigConst;
import io.sermant.flowcontrol.common.entity.FlowControlResult;
import io.sermant.flowcontrol.common.entity.FlowControlServiceMeta;
import io.sermant.flowcontrol.common.entity.HttpRequestEntity;
import io.sermant.flowcontrol.common.entity.RequestEntity.RequestType;
import io.sermant.flowcontrol.common.handler.retry.AbstractRetry;
import io.sermant.flowcontrol.common.handler.retry.Retry;
import io.sermant.flowcontrol.common.handler.retry.RetryContext;
import io.sermant.flowcontrol.service.InterceptorSupporter;

import org.springframework.http.HttpStatus;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * enhanced DispatcherServlet api interface, buried to define sentinel resources
 *
 * @author zhouss
 * @since 2022-02-11
 */
public class FeignRequestInterceptor extends InterceptorSupporter {
    private static final Logger LOGGER = LoggerFactory.getLogger();

    private final String className = FeignRequestInterceptor.class.getName();

    private final Exception defaultException = new Exception("request error");

    private final Retry retry = new FeignRetry();

    /**
     * http request data conversion: adapts plugin to service data passing Note that this method is not
     * extractable，Because host dependencies can only be loaded by this interceptor, pulling out results in classes not
     * being found.
     *
     * @param request request
     * @return HttpRequestEntity
     */
    private Optional<HttpRequestEntity> convertToHttpEntity(Request request) {
        if (request == null) {
            return Optional.empty();
        }
        try {
            final URL url = new URL(request.url());
            final HashMap<String, String> headers = new HashMap<>(request.headers().size());
            request.headers().forEach((headerName, headValue) -> headers.put(headerName, headValue.iterator().next()));
            return Optional.of(new HttpRequestEntity.Builder()
                    .setRequestType(RequestType.CLIENT)
                    .setApiPath(url.getPath())
                    .setHeaders(headers)
                    .setMethod(request.method())
                    .setServiceName(url.getHost())
                    .build());
        } catch (MalformedURLException e) {
            LOGGER.log(Level.WARNING, "Fail to convert Request to HttpRequestEntity, exception", e);
        }
        return Optional.of(new HttpRequestEntity());
    }

    @Override
    protected final ExecuteContext doBefore(ExecuteContext context) throws Exception {
        final FlowControlResult flowControlResult = new FlowControlResult();
        final Request request = getRequest(context);
        final Optional<HttpRequestEntity> httpRequestEntity = convertToHttpEntity(request);
        if (!httpRequestEntity.isPresent()) {
            return context;
        }
        chooseHttpService().onBefore(className, httpRequestEntity.get(), flowControlResult);
        if (flowControlResult.isSkip()) {
            final String responseMsg = flowControlResult.buildResponseMsg();
            context.skip(Response.builder()
                    .status(flowControlResult.getResponse().getCode())
                    .body(responseMsg, StandardCharsets.UTF_8)
                    .headers(Collections.emptyMap())
                    .reason(responseMsg)
                    .request(request)
                    .build());
        } else {
            executeWithRetryPolicy(context);
        }
        return context;
    }

    private Request getRequest(ExecuteContext context) {
        final Request request = (Request) context.getArguments()[0];
        String serviceName = FlowControlServiceMeta.getInstance().getServiceName();
        if (StringUtils.isEmpty(serviceName)) {
            return request;
        }
        final HashMap<String, Collection<String>> headers = new HashMap<>(request.headers());
        headers.put(ConfigConst.FLOW_REMOTE_SERVICE_NAME_HEADER_KEY, Collections.singletonList(serviceName));
        final Request newRequest = Request
                .create(request.method(), request.url(), headers, request.body(), request.charset());
        context.getArguments()[0] = newRequest;
        return newRequest;
    }

    private void executeWithRetryPolicy(ExecuteContext context) {
        final Object[] allArguments = context.getArguments();
        Request request = (Request) allArguments[0];
        Object result = context.getResult();
        Throwable ex = context.getThrowable();
        final Supplier<Object> retryFunc = createRetryFunc(context.getObject(),
                context.getMethod(), allArguments, context.getResult());
        RetryContext.INSTANCE.markRetry(retry);
        try {
            // first execution taking over the host logic
            result = retryFunc.get();
        } catch (Throwable throwable) {
            ex = throwable;
            log(throwable);
        }
        context.afterMethod(result, ex);
        try {
            final Optional<HttpRequestEntity> httpRequestEntity = convertToHttpEntity(request);
            if (!httpRequestEntity.isPresent()) {
                return;
            }
            RetryContext.INSTANCE.buildRetryPolicy(httpRequestEntity.get());
            final List<io.github.resilience4j.retry.Retry> handlers = getRetryHandler()
                    .getHandlers(httpRequestEntity.get());
            if (!handlers.isEmpty() && isNeedRetry(handlers.get(0), result, ex)) {
                result = handlers.get(0).executeCheckedSupplier(retryFunc::get);
            }
        } catch (Throwable throwable) {
            LOGGER.warning(String.format(Locale.ENGLISH,
                    "Failed to invoke method:%s for few times, reason:%s",
                    context.getMethod().getName(), getExMsg(throwable)));
        } finally {
            RetryContext.INSTANCE.remove();
        }
        context.skip(fixErrorResult(result, request, ex));
    }

    private Object fixErrorResult(Object result, Request request, Throwable ex) {
        if (result == null) {
            return Response.builder().request(request).reason(getExMsg(ex))
                    .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .headers(Collections.emptyMap())
                    .body(getExMsg(ex), StandardCharsets.UTF_8)
                    .build();
        }
        return result;
    }

    @Override
    protected ExecuteContext doThrow(ExecuteContext context) {
        chooseHttpService().onThrow(className, context.getThrowable());
        return context;
    }

    @Override
    protected final ExecuteContext doAfter(ExecuteContext context) {
        if (hasError(context)) {
            chooseHttpService().onThrow(className, defaultException);
        }
        chooseHttpService().onAfter(className, context.getResult());
        return context;
    }

    private boolean hasError(ExecuteContext context) {
        final Object result = context.getResult();
        if (result instanceof Response) {
            Response response = (Response) result;
            return response.status() >= HttpStatus.INTERNAL_SERVER_ERROR.value();
        }
        return false;
    }

    /**
     * feign retry
     *
     * @since 2022-02-11
     */
    public static class FeignRetry extends AbstractRetry {
        private static final String METHOD_KEY = "Response#status";

        @Override
        public Optional<String> getCode(Object result) {
            Optional<Object> resultOptional = getMethodResult(result, "status");
            return resultOptional.map(String::valueOf);
        }

        @Override
        public Optional<Set<String>> getHeaderNames(Object result) {
            Optional<Object> resultOptional = getMethodResult(result, "headers");
            if (!resultOptional.isPresent() || !(resultOptional.get() instanceof Map)) {
                return Optional.empty();
            }
            Map<?, ?> headers = (Map<?, ?>) resultOptional.get();
            Set<String> headerNames = new HashSet<>();
            for (Map.Entry<?, ?> entry : headers.entrySet()) {
                headerNames.add(entry.getKey().toString());
            }
            return Optional.of(headerNames);
        }

        private Optional<Object> getMethodResult(Object result, String methodName) {
            final Optional<Method> status = getInvokerMethod(result.getClass().getName() + METHOD_KEY, fn -> {
                final Method method;
                try {
                    method = result.getClass().getDeclaredMethod(methodName);
                    method.setAccessible(true);
                    return method;
                } catch (NoSuchMethodException ex) {
                    LOGGER.warning(String.format(Locale.ENGLISH,
                            "Can not find method status from response class %s", result.getClass().getName()));
                }
                return placeHolderMethod;
            });
            if (!status.isPresent()) {
                return Optional.empty();
            }
            try {
                return Optional.of(status.get().invoke(result));
            } catch (IllegalAccessException ex) {
                LOGGER.warning(String.format(Locale.ENGLISH, "Can not find method status from class [%s]!",
                        result.getClass().getCanonicalName()));
            } catch (InvocationTargetException ex) {
                LOGGER.warning(String.format(Locale.ENGLISH, "Invoking method status failed, reason: %s",
                        ex.getMessage()));
            }
            return Optional.empty();
        }

        @Override
        public Class<? extends Throwable>[] retryExceptions() {
            return getRetryExceptions();
        }

        @Override
        public RetryFramework retryType() {
            return RetryFramework.SPRING_CLOUD;
        }
    }
}
