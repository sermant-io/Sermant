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

import com.google.common.base.Optional;

import io.sermant.core.plugin.agent.entity.ExecuteContext;
import io.sermant.core.utils.CollectionUtils;
import io.sermant.flowcontrol.common.handler.retry.RetryContext;
import io.sermant.flowcontrol.common.handler.retry.policy.RetryOnUntriedPolicy;
import io.sermant.flowcontrol.common.handler.retry.policy.RetryPolicy;
import io.sermant.flowcontrol.service.InterceptorSupporter;

import java.util.ArrayList;
import java.util.List;

/**
 * Ribbon method select instance method to intercept. Obtain the last called service through interception, so that the
 * last called service can be selected again based on load balancing when retrying.
 *
 * @author zhouss
 * @see RetryOnUntriedPolicy
 * @since 2022-07-25
 */
public class SpringRibbonChooseServerInterceptor extends InterceptorSupporter {
    @Override
    protected ExecuteContext doBefore(ExecuteContext context) throws Exception {
        if (RetryContext.INSTANCE.isPolicyNeedRetry()) {
            removeRetriedServiceInstance(context);
        }
        return context;
    }

    @Override
    protected ExecuteContext doAfter(ExecuteContext context) throws Exception {
        if (RetryContext.INSTANCE.isPolicyNeedRetry()) {
            updateServiceInstance(context);
        }
        return context;
    }

    private void updateServiceInstance(ExecuteContext context) {
        final Object result = context.getResult();
        if (result instanceof Optional) {
            Optional<?> serverInstanceOption = (Optional<?>) result;
            if (!serverInstanceOption.isPresent()) {
                return;
            }
            RetryContext.INSTANCE.updateRetriedServiceInstance(serverInstanceOption.get());
        }
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
