/*
 * Copyright (C) 2022-2024 Sermant Authors. All rights reserved.
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

import com.huaweicloud.sermant.core.plugin.agent.entity.ExecuteContext;

import com.google.common.base.Optional;

/**
 * Ribbon method select instance method to intercept. Obtain the last called service through interception, so that the
 * last called service can be selected again based on load balancing when retrying.
 *
 * @author zhouss
 * @see com.huawei.flowcontrol.common.handler.retry.policy.RetryOnSamePolicy
 * @since 2022-07-25
 */
public class SpringRibbonChooseServerInterceptor extends InterceptorSupporter {
    @Override
    protected ExecuteContext doBefore(ExecuteContext context) throws Exception {
        if (RetryContext.INSTANCE.isPolicyNeedRetry()) {
            tryChangeServiceInstanceForRetry(context);
        }
        return context;
    }

    @Override
    protected ExecuteContext doAfter(ExecuteContext context) throws Exception {
        if (!RetryContext.INSTANCE.isPolicyNeedRetry()) {
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
            RetryContext.INSTANCE.updateServiceInstance(serverInstanceOption.get());
        }
    }

    private void tryChangeServiceInstanceForRetry(ExecuteContext context) {
        final RetryPolicy retryPolicy = RetryContext.INSTANCE.getRetryPolicy();
        if (retryPolicy != null && retryPolicy.getLastRetryServer() != null) {
            retryPolicy.retryMark();
            context.skip(Optional.of(retryPolicy.getLastRetryServer()));
        }
    }

    @Override
    protected boolean canInvoke(ExecuteContext context) {
        return true;
    }
}
