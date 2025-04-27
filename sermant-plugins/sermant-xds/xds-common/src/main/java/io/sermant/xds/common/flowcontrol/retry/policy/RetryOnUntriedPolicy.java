/*
 * Copyright (C) 2024-2025 Sermant Authors. All rights reserved.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package io.sermant.xds.common.flowcontrol.retry.policy;

import io.sermant.core.service.xds.entity.XdsRetryPolicy;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Retry on the Untried instance, regardless of thread safety, only on thread variables
 *
 * @author zhp
 * @since 2024-11-28
 */
public class RetryOnUntriedPolicy implements RetryPolicy {
    private final XdsRetryPolicy retryPolicy;

    private final Set<Object> retriedInstance;

    /**
     * retry constructor
     *
     * @param retryPolicy xds retry policy
     */
    public RetryOnUntriedPolicy(XdsRetryPolicy retryPolicy) {
        this.retryPolicy = retryPolicy;
        retriedInstance = new HashSet<>();
    }

    @Override
    public Set<Object> getAllRetriedInstance() {
        return retriedInstance;
    }

    @Override
    public void updateRetriedInstance(Object instance) {
        if (!this.retriedInstance.contains(instance)) {
            this.retriedInstance.add(instance);
        }
    }

    @Override
    public long getTryTimeout() {
        return this.retryPolicy.getPerTryTimeout();
    }

    @Override
    public List<String> getRetryConditions() {
        return this.retryPolicy.getRetryConditions();
    }

    @Override
    public long getMaxAttempts() {
        return this.retryPolicy.getMaxAttempts();
    }

    @Override
    public String getRetryPolicyName() {
        return this.retryPolicy.toString();
    }
}
