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

package com.huawei.flowcontrol.retry;

import com.huawei.flowcontrol.common.entity.HttpRequestEntity;
import com.huawei.flowcontrol.common.handler.retry.AbstractRetry;
import com.huawei.flowcontrol.common.handler.retry.Retry;
import com.huawei.flowcontrol.common.handler.retry.RetryContext;
import com.huawei.flowcontrol.service.InterceptorSupporter;
import com.huawei.sermant.core.common.LoggerFactory;
import com.huawei.sermant.core.plugin.agent.entity.ExecuteContext;

import feign.Request;

import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.function.Supplier;
import java.util.logging.Logger;

/**
 * DispatcherServlet 的 API接口增强 埋点定义sentinel资源
 *
 * @author zhouss
 * @since 2022-02-11
 */
public class FeignRequestInterceptor extends InterceptorSupporter {
    private static final Logger LOGGER = LoggerFactory.getLogger();

    private final Retry retry = new FeignRetry();

    /**
     * http请求数据转换 适应plugin -> service数据传递 注意，该方法不可抽出，由于宿主依赖仅可由该拦截器加载，因此抽出会导致找不到类
     *
     * @param request 请求
     * @return HttpRequestEntity
     */
    private HttpRequestEntity convertToHttpEntity(Request request) {
        if (request == null) {
            return null;
        }
        try {
            final URL url = new URL(request.url());
            final HashMap<String, String> headers = new HashMap<>(request.headers().size());
            request.headers().forEach((headerName, headValue) -> headers.put(headerName, headValue.iterator().next()));
            return new HttpRequestEntity(url.getPath(), headers, request.httpMethod().name());
        } catch (MalformedURLException ignored) {
            // ignored
        }
        return new HttpRequestEntity();
    }

    @Override
    protected final ExecuteContext doBefore(ExecuteContext context) {
        return context;
    }

    @SuppressWarnings("checkstyle:IllegalCatch")
    @Override
    protected final ExecuteContext doAfter(ExecuteContext context) {
        final Object[] allArguments = context.getArguments();
        Request request = (Request) allArguments[0];
        Object result = context.getResult();
        try {
            RetryContext.INSTANCE.markRetry(retry);
            final List<io.github.resilience4j.retry.Retry> handlers = retryHandler
                .getHandlers(convertToHttpEntity(request));
            if (!handlers.isEmpty() && needRetry(handlers.get(0), result, null)) {
                // 重试仅有一个策略
                final Supplier<Object> retryFunc = createRetryFunc(context.getObject(),
                    context.getMethod(), allArguments, context.getResult());
                result = handlers.get(0).executeCheckedSupplier(retryFunc::get);
            }
        } catch (Throwable throwable) {
            LOGGER.warning(String.format(Locale.ENGLISH,
                "Failed to invoke method:%s for few times, reason:%s",
                context.getMethod().getName(), throwable.getCause()));
        } finally {
            RetryContext.INSTANCE.removeRetry();
        }
        context.changeResult(result);
        return context;
    }

    public static class FeignRetry extends AbstractRetry {
        private static final String METHOD_KEY = "Response#status";

        @Override
        @SuppressWarnings("checkstyle:IllegalCatch")
        public String getCode(Object result) {
            final Method status = getInvokerMethod(METHOD_KEY, fn -> {
                final Method method;
                try {
                    method = result.getClass().getDeclaredMethod("status");
                    method.setAccessible(true);
                    return method;
                } catch (NoSuchMethodException ignored) {
                    // ignored
                }
                return null;
            });
            if (status == null) {
                return null;
            }
            try {
                return String.valueOf(status.invoke(result));
            } catch (Exception ignored) {
                // ignored
            }
            return null;
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
