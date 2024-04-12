/*
 * Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.huawei.discovery.retry;

import com.huawei.discovery.entity.Recorder;
import com.huawei.discovery.retry.config.RetryConfig;

/**
 * Retryer
 *
 * @author zhouss
 * @since 2022-09-28
 */
public interface Retry {
    /**
     * Create a retryer
     *
     * @param retryConfig Retry the configuration
     * @return Retry
     */
    static Retry create(RetryConfig retryConfig) {
        return new DefaultRetryImpl(retryConfig, retryConfig.getName());
    }

    /**
     * Get the retry configuration
     *
     * @return Retry
     */
    RetryConfig config();

    /**
     * Configuration Name
     *
     * @return The name of the retryer
     */
    String name();

    /**
     * Create a context
     *
     * @param <T> The type of record
     * @return Retry the context
     */
    <T extends Recorder> RetryContext<T> context();

    /**
     * Retry the context
     *
     * @param <T> Logger type
     * @since 2022-09-28
     */
    interface RetryContext<T extends Recorder> {
        /**
         * Call preferential
         *
         * @param serviceInstanceStats Select the instance for which you want to call
         */
        void onBefore(T serviceInstanceStats);

        /**
         * Invoke result validation
         *
         * @param serviceInstanceStats Select the instance for which you want to call
         * @param result Invoke the result
         * @param consumeTimeMs The time consumed by the call
         * @return true indicates that the retry is passed or the maximum number of retries is reached, and false
         * indicates that the next retry is required
         */
        boolean onResult(T serviceInstanceStats, Object result, long consumeTimeMs);

        /**
         * Invoke exception validation
         *
         * @param serviceInstanceStats Select the instance for which you want to call
         * @param ex Called when an exception is called
         * @param consumeTimeMs The time consumed by the call
         * @throws RetryException An exception is thrown when the exception retry condition is not met
         */
        void onError(T serviceInstanceStats, Throwable ex, long consumeTimeMs) throws RetryException;

        /**
         * Finally, the method is called after the retry is completely over
         *
         * @param serviceInstanceStats Select an instance
         */
        void onComplete(T serviceInstanceStats);
    }
}
