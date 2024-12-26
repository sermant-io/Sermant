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

import io.sermant.core.common.LoggerFactory;
import io.sermant.core.plugin.agent.entity.ExecuteContext;
import io.sermant.core.utils.CollectionUtils;
import io.sermant.core.utils.ReflectUtils;
import io.sermant.flowcontrol.common.handler.retry.RetryContext;
import io.sermant.flowcontrol.common.handler.retry.policy.RetryPolicy;
import io.sermant.flowcontrol.service.InterceptorSupporter;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * springCloud loadbalancer select instance method to intercept. Obtain the last called service through interception, so
 * that the last called service can be selected again based on load balancing when retrying.
 *
 * @author zhouss
 * @see io.sermant.flowcontrol.common.handler.retry.policy.RetryOnUntriedPolicy
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
            removeRetriedServiceInstance(context);
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

    private void updateServiceInstance(Object result) {
        final Optional<Object> getServer = ReflectUtils.invokeMethod(result, "getServer", null, null);
        if (!getServer.isPresent()) {
            return;
        }
        RetryContext.INSTANCE.updateRetriedServiceInstance(getServer.get());
    }

    private boolean isTarget(Object result) {
        final String name = result.getClass().getName();
        return DEFAULT_RESPONSE_CLASS.equals(name) || DEFAULT_RESPONSE_REACTIVE_CLASS.equals(name);
    }

    @Override
    protected boolean canInvoke(ExecuteContext context) {
        return true;
    }

    /**
     * remove retried instance
     *
     * @param context The execution context of the Interceptor
     */
    public void removeRetriedServiceInstance(ExecuteContext context) {
        Object[] arguments = context.getArguments();
        if (arguments == null || arguments.length == 0 || !(arguments[0] instanceof List)) {
            return;
        }
        final RetryPolicy retryPolicy = RetryContext.INSTANCE.getRetryPolicy();
        if (retryPolicy == null || CollectionUtils.isEmpty(retryPolicy.getAllRetriedInstance())) {
            return;
        }
        retryPolicy.retryMark();
        List<?> instances = new ArrayList<>((List<?>) arguments[0]);
        for (Object instance : retryPolicy.getAllRetriedInstance()) {
            instances.remove(instance);
        }
        if (!instances.isEmpty()) {
            arguments[0] = instances;
        }
    }
}
