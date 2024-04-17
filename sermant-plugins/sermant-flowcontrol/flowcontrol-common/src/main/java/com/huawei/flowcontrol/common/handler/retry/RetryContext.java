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

package com.huawei.flowcontrol.common.handler.retry;

import com.huawei.flowcontrol.common.core.RuleUtils;
import com.huawei.flowcontrol.common.core.resolver.RetryResolver;
import com.huawei.flowcontrol.common.core.rule.RetryRule;
import com.huawei.flowcontrol.common.entity.HttpRequestEntity;
import com.huawei.flowcontrol.common.handler.retry.policy.RetryOnSamePolicy;
import com.huawei.flowcontrol.common.handler.retry.policy.RetryPolicy;

import java.util.List;

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
    public boolean isPolicyNeedRetry() {
        final RetryPolicy retryPolicy = getRetryPolicy();
        if (retryPolicy == null) {
            return false;
        }
        return retryPolicy.isRetry() && retryPolicy.needRetry();
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
    public void updateServiceInstance(Object serviceInstance) {
        final RetryPolicy retryPolicy = getRetryPolicy();
        if (retryPolicy == null) {
            return;
        }
        retryPolicy.update(serviceInstance);
        retryPolicy.retryMark();
    }

    /**
     * buildRetryPolicy
     *
     * @param retryRule retry rule
     */
    public void buildRetryPolicy(RetryRule retryRule) {
        policyThreadLocal.set(new RetryOnSamePolicy(retryRule.getRetryOnSame()));
    }

    /**
     * build test strategy
     *
     * @param requestEntity request body
     */
    public void buildRetryPolicy(HttpRequestEntity requestEntity) {
        final List<RetryRule> rule = RuleUtils.getRule(requestEntity, RetryResolver.CONFIG_KEY,
                RetryRule.class);
        if (!rule.isEmpty()) {
            RetryContext.INSTANCE.buildRetryPolicy(rule.get(0));
        }
    }
}
