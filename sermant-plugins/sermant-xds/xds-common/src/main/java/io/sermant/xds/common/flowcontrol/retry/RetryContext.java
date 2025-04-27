/*
 * Copyright (C) 2022-2025 Huawei Technologies Co., Ltd. All rights reserved.
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

package io.sermant.xds.common.flowcontrol.retry;

import io.sermant.core.service.xds.entity.XdsRetryPolicy;
import io.sermant.xds.common.flowcontrol.retry.policy.RetryOnUntriedPolicy;
import io.sermant.xds.common.flowcontrol.retry.policy.RetryPolicy;

/**
 * Retry context, used to manage retry policies based on different host framework types
 *
 * @author zhouss
 * @since 2022-01-26
 */
public enum RetryContext {
    /**
     * singleton
     */
    INSTANCE;

    private final ThreadLocal<Retry> retryThreadLocal = new ThreadLocal<>();

    private final ThreadLocal<RetryPolicy> policyThreadLocal = new ThreadLocal<>();

    /**
     * gets the retry mechanism for the current thread
     *
     * @return the retry mechanism for the current thread
     */
    public Retry getRetry() {
        return retryThreadLocal.get();
    }

    /**
     * mark the current thread retry
     *
     * @param retry retry
     */
    public void markRetry(Retry retry) {
        retryThreadLocal.set(retry);
    }

    /**
     * remove thread variable
     */
    public void remove() {
        retryThreadLocal.remove();
        policyThreadLocal.remove();
    }

    /**
     * mark or not retry
     *
     * @return mark or not retry
     */
    public boolean isMarkedRetry() {
        return retryThreadLocal.get() != null;
    }

    /**
     * Whether to perform retry in the current retry policy
     *
     * @return Whether to perform retry in the current retry policy
     */
    public boolean isRetriedRequest() {
        final RetryPolicy retryPolicy = getRetryPolicy();

        // The retry policy will be cached in the thread-local variable only after the first invocation is executed.
        if (retryPolicy == null) {
            return false;
        }
        return true;
    }

    /**
     * gets the retry policy for the current thread
     *
     * @return the retry policy for the current thread
     */
    public RetryPolicy getRetryPolicy() {
        return policyThreadLocal.get();
    }

    /**
     * Update the call service instance of the retry policy
     *
     * @param serviceInstance service instance
     */
    public void updateRetriedServiceInstance(Object serviceInstance) {
        final RetryPolicy retryPolicy = getRetryPolicy();
        if (retryPolicy == null) {
            return;
        }
        retryPolicy.updateRetriedInstance(serviceInstance);
    }

    /**
     * build retry policy
     *
     * @param retryPolicy retry policy information
     */
    public void buildXdsRetryPolicy(XdsRetryPolicy retryPolicy) {
        policyThreadLocal.set(new RetryOnUntriedPolicy(retryPolicy));
    }
}
