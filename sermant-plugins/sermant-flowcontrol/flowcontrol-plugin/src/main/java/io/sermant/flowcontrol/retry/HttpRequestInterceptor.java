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

import io.github.resilience4j.retry.Retry;
import io.sermant.core.common.LoggerFactory;
import io.sermant.core.plugin.agent.entity.ExecuteContext;
import io.sermant.flowcontrol.common.config.ConfigConst;
import io.sermant.flowcontrol.common.entity.FlowControlResult;
import io.sermant.flowcontrol.common.entity.FlowControlServiceMeta;
import io.sermant.flowcontrol.common.entity.HttpRequestEntity;
import io.sermant.flowcontrol.common.entity.RequestEntity.RequestType;
import io.sermant.flowcontrol.common.handler.retry.AbstractRetry;
import io.sermant.flowcontrol.common.handler.retry.RetryContext;
import io.sermant.flowcontrol.common.util.XdsThreadLocalUtil;
import io.sermant.flowcontrol.inject.DefaultClientHttpResponse;
import io.sermant.flowcontrol.inject.RetryClientHttpResponse;
import io.sermant.flowcontrol.service.InterceptorSupporter;

import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.logging.Logger;

/**
 * enhanced DispatcherServlet api interface, buried to define sentinel resources
 *
 * @author zhouss
 * @since 2022-02-11
 */
public class HttpRequestInterceptor extends InterceptorSupporter {
    private static final Logger LOGGER = LoggerFactory.getLogger();

    private final String className = HttpRequestInterceptor.class.getName();

    private final Exception defaultException = new Exception("request error");

    private final io.sermant.flowcontrol.common.handler.retry.Retry retry = new HttpRetry();

    /**
     * http request data conversion: adapts plugin to service data passing Note that this method is not
     * extractable，Because host dependencies can only be loaded by this interceptor, pulling out results in classes not
     * being found.
     *
     * @param request request
     * @return HttpRequestEntity
     */
    private Optional<HttpRequestEntity> convertToHttpEntity(HttpRequest request) {
        if (request == null) {
            return Optional.empty();
        }
        return Optional.of(new HttpRequestEntity.Builder()
                .setRequestType(RequestType.CLIENT)
                .setApiPath(request.getURI().getPath())
                .setHeaders(request.getHeaders().toSingleValueMap())
                .setMethod(request.getMethod().name())
                .setServiceName(request.getURI().getHost())
                .build());
    }

    @Override
    protected final ExecuteContext doBefore(ExecuteContext context) {
        final FlowControlResult flowControlResult = new FlowControlResult();
        final HttpRequest request = (HttpRequest) context.getObject();
        request.getHeaders().put(ConfigConst.FLOW_REMOTE_SERVICE_NAME_HEADER_KEY,
                Collections.singletonList(FlowControlServiceMeta.getInstance().getServiceName()));
        final Optional<HttpRequestEntity> httpRequestEntity = convertToHttpEntity(request);
        if (!httpRequestEntity.isPresent()) {
            return context;
        }
        chooseHttpService().onBefore(className, httpRequestEntity.get(), flowControlResult);
        if (flowControlResult.isSkip()) {
            context.skip(new DefaultClientHttpResponse(flowControlResult));
        } else {
            tryExeWithRetry(context, httpRequestEntity.get());
        }
        return context;
    }

    private void tryExeWithRetry(ExecuteContext context, HttpRequestEntity httpRequestEntity) {
        final Object[] allArguments = context.getArguments();
        final HttpRequest request = (HttpRequest) context.getObject();
        Object result;
        Throwable ex = context.getThrowable();
        final Supplier<Object> retryFunc = createRetryFunc(context.getObject(),
                context.getMethod(), allArguments, context.getResult());
        RetryContext.INSTANCE.markRetry(retry);
        try {
            // first execution taking over the host logic
            result = retryFunc.get();
        } catch (Throwable throwable) {
            ex = getRealCause(throwable);
            result = new RetryClientHttpResponse(getExMsg(ex), HttpStatus.INTERNAL_SERVER_ERROR.value());
            log(throwable);
        }
        context.afterMethod(result, ex);
        try {
            RetryContext.INSTANCE.buildRetryPolicy(httpRequestEntity);
            final List<Retry> handlers = getRetryHandler().getHandlers(httpRequestEntity);
            if (!handlers.isEmpty() && needRetry(handlers.get(0), result, ex)) {
                // retry only one policy
                request.getHeaders().add(RETRY_KEY, RETRY_VALUE);
                result = handlers.get(0).executeCheckedSupplier(retryFunc::get);
                request.getHeaders().remove(RETRY_KEY);
            }
        } catch (Throwable throwable) {
            LOGGER.warning(String.format(Locale.ENGLISH,
                    "Failed to invoke method:%s for few times, reason:%s",
                    context.getMethod().getName(), getExMsg(throwable)));
        } finally {
            RetryContext.INSTANCE.remove();
        }
        context.skip(result);
    }

    @Override
    protected ExecuteContext doThrow(ExecuteContext context) {
        chooseHttpService().onThrow(className, context.getThrowable());
        XdsThreadLocalUtil.removeSendByteFlag();
        return context;
    }

    @Override
    protected final ExecuteContext doAfter(ExecuteContext context) throws IOException {
        if (hasError(context.getResult())) {
            // Since the interception is based on the current method, even if there is an exception,
            // it will not be thrown, and will only be returned in the form of an error code,
            // so the doThrow method will not be called, so it is triggered by manual calling here.
            chooseHttpService().onThrow(className, defaultException);
        }
        chooseHttpService().onAfter(className, context.getResult());
        XdsThreadLocalUtil.removeSendByteFlag();
        return context;
    }

    private boolean hasError(Object result) throws IOException {
        if (result instanceof ClientHttpResponse) {
            ClientHttpResponse response = (ClientHttpResponse) result;
            return response.getRawStatusCode() >= HttpStatus.INTERNAL_SERVER_ERROR.value();
        }
        return false;
    }

    /**
     * Http request retry
     *
     * @since 2022-02-21
     */
    public static class HttpRetry extends AbstractRetry {
        private static final String METHOD_KEY = "ClientHttpResponse#getRawStatusCode";

        @Override
        public Optional<String> getCode(Object result) {
            Optional<Object> resultOptional = getMethodResult(result, "getRawStatusCode");
            return resultOptional.map(String::valueOf);
        }

        @Override
        public Optional<Set<String>> getHeaderNames(Object result) {
            Optional<Object> resultOptional = getMethodResult(result, "getHeaders");
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

        @Override
        public Class<? extends Throwable>[] retryExceptions() {
            return getRetryExceptions();
        }

        @Override
        public RetryFramework retryType() {
            return RetryFramework.SPRING_CLOUD;
        }

        private Optional<Object> getMethodResult(Object result, String methodName) {
            final Optional<Method> getRawStatusCode = getInvokerMethod(result.getClass().getName() + METHOD_KEY, fn -> {
                try {
                    final Method method = result.getClass().getDeclaredMethod(methodName);
                    method.setAccessible(true);
                    return method;
                } catch (NoSuchMethodException ex) {
                    LOGGER.warning(String.format(Locale.ENGLISH,
                            "Can not find method getRawStatusCode from response class %s",
                            result.getClass().getName()));
                }
                return placeHolderMethod;
            });
            if (!getRawStatusCode.isPresent()) {
                return Optional.empty();
            }
            try {
                return Optional.of(getRawStatusCode.get().invoke(result));
            } catch (IllegalAccessException ex) {
                LOGGER.warning(String.format(Locale.ENGLISH,
                        "Can not find method getRawStatusCode from class [%s]!",
                        result.getClass().getCanonicalName()));
            } catch (InvocationTargetException ex) {
                LOGGER.warning(String.format(Locale.ENGLISH, "Invoking method getRawStatusCode failed, reason: %s",
                        ex.getMessage()));
            }
            return Optional.empty();
        }
    }
}
