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
import com.huawei.flowcontrol.common.handler.retry.RetryProcessor;
import com.huawei.flowcontrol.service.InterceptorSupporter;
import com.huawei.sermant.core.plugin.agent.entity.ExecuteContext;
import com.huawei.sermant.core.plugin.agent.interceptor.Interceptor;

import org.springframework.http.HttpRequest;

import java.lang.reflect.Method;
import java.util.List;

/**
 * DispatcherServlet 的 API接口增强 埋点定义sentinel资源
 *
 * @author zhouss
 * @since 2022-02-11
 */
public class HttpRequestInterceptor extends InterceptorSupporter implements Interceptor {
    private final Retry retry = new HttpRetry();

    /**
     * http请求数据转换 适应plugin -> service数据传递 注意，该方法不可抽出，由于宿主依赖仅可由该拦截器加载，因此抽出会导致找不到类
     *
     * @param request 请求
     * @return HttpRequestEntity
     */
    private HttpRequestEntity convertToHttpEntity(HttpRequest request) {
        if (request == null) {
            return null;
        }
        return new HttpRequestEntity(request.getURI().getPath(), request.getHeaders().toSingleValueMap(),
            request.getMethod().name());
    }

    @Override
    public ExecuteContext before(ExecuteContext context) throws Exception {
        RetryContext.INSTANCE.setRetry(retry);
        return context;
    }

    @Override
    public ExecuteContext after(ExecuteContext context) throws Exception {
        if (!RetryContext.INSTANCE.isReady()) {
            return context;
        }
        final Object[] allArguments = context.getArguments();
        final HttpRequest request = (HttpRequest) allArguments[0];
        final List<String> retryHeaders = request.getHeaders().get(RETRY_KEY);
        Object result = context.getResult();
        if (retryHeaders == null || retryHeaders.isEmpty()) {
            final List<RetryProcessor> handlers = retryHandler.getHandlers(convertToHttpEntity(request));
            if (!handlers.isEmpty()) {
                // 重试仅有一个策略
                request.getHeaders().add(RETRY_KEY, RETRY_VALUE);
                result = handlers.get(0).checkAndRetry(result,
                    createRetryFunc(context.getObject(), context.getMethod(), allArguments, result), null);
                request.getHeaders().remove(RETRY_KEY);
            }
        }
        context.changeResult(result);
        RetryContext.INSTANCE.removeRetry();
        return context;
    }

    @Override
    public ExecuteContext onThrow(ExecuteContext context) throws Exception {
        RetryContext.INSTANCE.removeRetry();
        return context;
    }

    public static class HttpRetry extends AbstractRetry {
        private static final String METHOD_KEY = "ClientHttpResponse#getRawStatusCode";

        @Override
        @SuppressWarnings("checkstyle:IllegalCatch")
        protected String getCode(Object result) {
            final Method getRawStatusCode = getInvokerMethod(METHOD_KEY, fn -> {
                try {
                    final Method method = result.getClass().getDeclaredMethod("getRawStatusCode");
                    method.setAccessible(true);
                    return method;
                } catch (NoSuchMethodException ignored) {
                    // ignored
                }
                return null;
            });
            if (getRawStatusCode == null) {
                return null;
            }
            try {
                return String.valueOf(getRawStatusCode.invoke(result));
            } catch (Exception ignored) {
                // ignored
                return null;
            }
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
