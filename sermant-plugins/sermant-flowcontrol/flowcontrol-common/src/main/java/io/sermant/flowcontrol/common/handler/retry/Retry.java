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

import java.util.Set;

/**
 * define retry
 *
 * @author zhouss
 * @since 2022-01-26
 */
public interface Retry {
    /**
     * needToRetry
     *
     * @param statusList List of status codes, valid only for http applications
     * @param result responseResult
     * @return retryOrNot
     */
    boolean needRetry(Set<String> statusList, Object result);

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
        APACHE_DUBBO
    }
}
