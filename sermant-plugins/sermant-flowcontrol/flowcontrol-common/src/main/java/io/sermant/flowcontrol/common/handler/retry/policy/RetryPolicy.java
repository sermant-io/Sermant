/*
 * Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
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

package io.sermant.flowcontrol.common.handler.retry.policy;

import java.util.Set;

/**
 * retry strategy
 *
 * @author zhouss
 * @since 2022-07-25
 */
public interface RetryPolicy {
    /**
     * is reached max attempts
     *
     * @return retry or not
     */
    boolean isReachedRetryThreshold();

    /**
     * retry mark
     */
    void retryMark();

    /**
     * whether the system is in retry state
     *
     * @return whether the status is retry
     */
    boolean isRetry();

    /**
     * Gets All retry instance
     *
     * @return retry instance
     */
    Set<Object> getAllRetriedInstance();

    /**
     * update retry instance
     *
     * @param instance instance
     */
    void updateRetriedInstance(Object instance);
}
