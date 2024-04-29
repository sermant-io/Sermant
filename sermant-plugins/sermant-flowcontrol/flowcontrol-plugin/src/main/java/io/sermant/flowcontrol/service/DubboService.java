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

package io.sermant.flowcontrol.service;

import io.sermant.flowcontrol.common.entity.FlowControlResult;
import io.sermant.flowcontrol.common.entity.RequestEntity;

/**
 * dubbo intercept
 *
 * @author zhouss
 * @since 2022-01-25
 */
public interface DubboService {
    /**
     * preintercept
     *
     * @param sourceName From the origin, it is recommended to name the target interceptor permission. This value is
     * used to distinguish thread variables when spanning multiple interceptors.
     *
     * @param requestEntity requestInformation
     * @param fixedResult fixedResult
     * @param isProvider isProvider
     */
    void onBefore(String sourceName, RequestEntity requestEntity, FlowControlResult fixedResult, boolean isProvider);

    /**
     * postsetMethod
     *
     * @param sourceName From the origin, it is recommended to name the target interceptor permission. This value is
     * used to distinguish thread variables when spanning multiple interceptors.
     *
     * @param result response result
     * @param isProvider is provider
     * @param hasException whether a call exception occurs， The after method is called when an exception occurs in the
     * dubbo scenario
     */
    void onAfter(String sourceName, Object result, boolean isProvider, boolean hasException);

    /**
     * exception throwing method
     *
     * @param sourceName From the origin, it is recommended to name the target interceptor permission. This value is
     * used to distinguish thread variables when spanning multiple interceptors.
     *
     * @param throwable exception message
     * @param isProvider is provider
     * @return need to retry
     */
    boolean onThrow(String sourceName, Throwable throwable, boolean isProvider);
}
