/*
 * Copyright (C) 2021-2024 Sermant Authors. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.huawei.flowcontrol.common.core.rule;

/**
 * isolation bin rule
 *
 * @author zhouss
 * @since 2021-11-15
 */
public class BulkheadRule extends AbstractRule {
    /**
     * maximum concurrency
     */
    public static final int DEFAULT_MAX_CONCURRENT_CALLS = 1000;

    /**
     * default maximum wait time
     */
    public static final long DEFAULT_MAX_WAIT_DURATION_MS = 0L;

    /**
     * maximum concurrency
     */
    private int maxConcurrentCalls = DEFAULT_MAX_CONCURRENT_CALLS;

    /**
     * maximum wait time
     */
    private String maxWaitDuration = String.valueOf(DEFAULT_MAX_WAIT_DURATION_MS);

    /**
     * Maximum wait time after conversion
     */
    private long parsedMaxWaitDuration = DEFAULT_MAX_WAIT_DURATION_MS;

    @Override
    public boolean isInValid() {
        if (maxConcurrentCalls < 0) {
            return true;
        }
        if (parsedMaxWaitDuration < 0) {
            return true;
        }
        return super.isInValid();
    }

    public long getParsedMaxWaitDuration() {
        return parsedMaxWaitDuration;
    }

    public int getMaxConcurrentCalls() {
        return maxConcurrentCalls;
    }

    public void setMaxConcurrentCalls(int maxConcurrentCalls) {
        this.maxConcurrentCalls = maxConcurrentCalls;
    }

    public String getMaxWaitDuration() {
        return maxWaitDuration;
    }

    /**
     * the maximum waiting time is set
     *
     * @param maxWaitDuration maximum waiting time
     */
    public void setMaxWaitDuration(String maxWaitDuration) {
        this.maxWaitDuration = maxWaitDuration;
        this.parsedMaxWaitDuration = parseLongTime(maxWaitDuration, DEFAULT_MAX_WAIT_DURATION_MS);
    }
}
