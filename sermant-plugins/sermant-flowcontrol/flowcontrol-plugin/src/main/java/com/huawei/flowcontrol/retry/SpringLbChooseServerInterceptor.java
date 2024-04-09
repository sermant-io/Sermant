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

import com.huawei.flowcontrol.common.handler.retry.RetryContext;
import com.huawei.flowcontrol.common.handler.retry.policy.RetryPolicy;
import com.huawei.flowcontrol.service.InterceptorSupporter;

import com.huaweicloud.sermant.core.common.LoggerFactory;
import com.huaweicloud.sermant.core.plugin.agent.entity.ExecuteContext;
import com.huaweicloud.sermant.core.utils.ReflectUtils;

import org.springframework.cloud.client.ServiceInstance;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Locale;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * springCloud loadbalancer select instance method to intercept. Obtain the last called service through interception, so
 * that the last called service can be selected again based on load balancing when retrying.
 *
 * @author zhouss
 * @see com.huawei.flowcontrol.common.handler.retry.policy.RetryOnSamePolicy
 * @since 2022-07-25
 */
public class SpringLbChooseServerInterceptor extends InterceptorSupporter {
    private static final Logger LOGGER = LoggerFactory.getLogger();

    private static final String DEFAULT_RESPONSE_REACTIVE_CLASS =
            "org.springframework.cloud.client.loadbalancer.reactive.DefaultResponse";

    private static final String RESPONSE_REACTIVE_CLASS =
            "org.springframework.cloud.client.loadbalancer.reactive.Response";

    private static final String DEFAULT_RESPONSE_CLASS =
            "org.springframework.cloud.client.loadbalancer.DefaultResponse";

    private static final String RESPONSE_CLASS =
            "org.springframework.cloud.client.loadbalancer.Response";

    @Override
    protected ExecuteContext doBefore(ExecuteContext context) throws Exception {
        if (RetryContext.INSTANCE.isPolicyNeedRetry()) {
            tryChangeServiceInstanceForRetry(context);
        }
        return context;
    }

    @Override
    protected ExecuteContext doAfter(ExecuteContext context) throws Exception {
        final Object result = context.getResult();
        if (!isTarget(result)) {
            return context;
        }
        if (!RetryContext.INSTANCE.isPolicyNeedRetry()) {
            updateServiceInstance(result);
        }
        return context;
    }

    private void tryChangeServiceInstanceForRetry(ExecuteContext context) {
        final RetryPolicy retryPolicy = RetryContext.INSTANCE.getRetryPolicy();
        if (retryPolicy != null && retryPolicy.getLastRetryServer() != null) {
            retryPolicy.retryMark();
            final Optional<Object> result = buildResult(retryPolicy.getLastRetryServer(),
                    context.getMethod().getReturnType().getName());
            if (!result.isPresent()) {
                return;
            }
            context.skip(result.get());
        }
    }

    private Optional<Object> buildResult(Object lastServer, String responseClassName) {
        String defaultResponseClazz = null;
        if (RESPONSE_CLASS.equals(responseClassName)) {
            defaultResponseClazz = DEFAULT_RESPONSE_CLASS;
        } else if (RESPONSE_REACTIVE_CLASS.equals(responseClassName)) {
            defaultResponseClazz = DEFAULT_RESPONSE_REACTIVE_CLASS;
        }
        if (defaultResponseClazz == null) {
            return Optional.empty();
        }
        final ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        try {
            final Class<?> clazz = contextClassLoader.loadClass(defaultResponseClazz);
            final Constructor<?> declaredConstructor = clazz.getDeclaredConstructor(ServiceInstance.class);
            return Optional.of(declaredConstructor.newInstance(lastServer));
        } catch (ClassNotFoundException | NoSuchMethodException | InstantiationException | IllegalAccessException
                | InvocationTargetException exception) {
            LOGGER.warning(String.format(Locale.ENGLISH,
                    "Can not create loadbalancer response for retry! className: [%s]", defaultResponseClazz));
        }
        return Optional.empty();
    }

    private void updateServiceInstance(Object result) {
        final Optional<Object> getServer = ReflectUtils.invokeMethod(result, "getServer", null, null);
        if (!getServer.isPresent()) {
            return;
        }
        RetryContext.INSTANCE.updateServiceInstance(getServer.get());
    }

    private boolean isTarget(Object result) {
        final String name = result.getClass().getName();
        return DEFAULT_RESPONSE_CLASS.equals(name) || DEFAULT_RESPONSE_REACTIVE_CLASS.equals(name);
    }

    @Override
    protected boolean canInvoke(ExecuteContext context) {
        return true;
    }
}
