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

package io.sermant.flowcontrol.common.handler.retry;

import io.sermant.core.service.xds.entity.XdsRetryPolicy;

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
     * Retry based on the request result. Retrying is required if the request status is in the statusList.
     *
     * @param statusList List of status codes, valid only for http applications
     * @param result responseResult
     * @return retryOrNot
     */
    boolean isNeedRetry(Set<String> statusList, Object result);

    /**
     * Retry based on the request result. If the request result meets the retry conditions in the retry policy,
     * a retry will be executed
     *
     * @param result responseResult
     * @param retryPolicy retry policy information
     * @return retryOrNot
     */
    boolean isNeedRetry(Object result, XdsRetryPolicy retryPolicy);

    /**
     * Retry based on the throwable. If the throwable during the execution of the request method meets the retry
     * conditions in the retry policy, a retry will be executed
     *
     * @param throwable Exception thrown during retry
     * @param retryPolicy Xds Retry Policy information
     * @return retryOrNot
     */
    boolean isNeedRetry(Throwable throwable, XdsRetryPolicy retryPolicy);

    /**
     * define which exceptions need to be retried
     *
     * @return exception that needs to be retried
     */
    Class<? extends Throwable>[] retryExceptions();

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
    Optional<String> getCode(Object result);

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
         * spring retry
         */
        SPRING_CLOUD,

        /**
         * alibaba dubbo retry
         */
        ALIBABA_DUBBO,

        /**
         * apache dubbo retry
         */
        APACHE_DUBBO,

        /**
         * Spring retry
         */
        SPRING;
    }
}
