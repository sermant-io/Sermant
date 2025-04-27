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

import java.util.Optional;
import java.util.Set;

/**
 * define retry
 *
 * @author zhouss
 * @since 2022-01-26
 */
public interface Retry {
    /**
     * Retry based on the request result. If the request result meets the retry conditions in the retry policy,
     * a retry will be executed
     *
     * @param result responseResult
     * @return retryOrNot
     */
    boolean isNeedRetry(Object result);

    /**
     * Retry based on the throwable. If the throwable during the execution of the request method meets the retry
     * conditions in the retry policy, a retry will be executed
     *
     * @param throwable Exception thrown during retry
     * @return retryOrNot
     */
    boolean isNeedRetry(Throwable throwable);

    /**
     * retry frame type
     *
     * @return frame
     */
    RetryFramework retryType();

    /**
     * get status code
     *
     * @param result interface response result
     * @return response status code
     */
    Optional<String> getStatusCode(Object result);

    /**
     * get header
     *
     * @param result interface response result
     * @return response header names
     */
    Optional<Set<String>> getHeaderNames(Object result);

    /**
     * retryFrame
     *
     * @since 2022-01-22
     */
    enum RetryFramework {
        /**
         * Spring retry
         */
        SPRING;
    }
}
