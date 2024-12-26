/*
 * Copyright (C) 2024-2024 Sermant Authors. All rights reserved.
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

package io.sermant.flowcontrol.common.handler.retry.policy;

import java.util.HashSet;
import java.util.Set;

/**
 * Retry on the Untried instance, regardless of thread safety, only on thread variables
 *
 * @author zhp
 * @since 2024-11-28
 */
public class RetryOnUntriedPolicy implements RetryPolicy {
    private final int attempts;

    private final Set<Object> retriedInstance;

    private int hasTriedCount;

    private boolean isRetry;

    private boolean isFirstRequest = true;

    /**
     * retry constructor
     *
     * @param attempts Maximum Retry Count
     */
    public RetryOnUntriedPolicy(int attempts) {
        this.attempts = attempts;
        retriedInstance = new HashSet<>();
    }

    @Override
    public boolean isReachedRetryThreshold() {
        return hasTriedCount < attempts;
    }

    @Override
    public void retryMark() {
        if (!isFirstRequest) {
            this.hasTriedCount++;
        }
        this.isRetry = true;
        isFirstRequest = false;
    }

    @Override
    public boolean isRetry() {
        return isRetry;
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
}
