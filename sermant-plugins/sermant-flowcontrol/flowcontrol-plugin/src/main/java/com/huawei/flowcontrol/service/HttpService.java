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

package com.huawei.flowcontrol.service;

import com.huawei.flowcontrol.common.entity.FlowControlResult;
import com.huawei.flowcontrol.common.entity.RequestEntity;

/**
 * http intercept
 *
 * @author zhouss
 * @since 2022-01-25
 */
public interface HttpService {
    /**
     * preintercept
     *
     * @param sourceName From the origin, it is recommended to name the target interceptor permission. This value is
     * used to distinguish thread variables when spanning multiple interceptors.
     *
     * @param requestEntity request information
     * @param fixedResult fixed result
     */
    void onBefore(String sourceName, RequestEntity requestEntity, FlowControlResult fixedResult);

    /**
     * postset method
     *
     * @param sourceName From the origin, it is recommended to name the target interceptor permission. This value is
     * used to distinguish thread variables when spanning multiple interceptors.
     *
     * @param result response result
     */
    void onAfter(String sourceName, Object result);

    /**
     * exception throwing method
     *
     * @param sourceName From the origin, it is recommended to name the target interceptor permission. This value is
     * used to distinguish thread variables when spanning multiple interceptors.
     *
     * @param throwable exception message
     */
    void onThrow(String sourceName, Throwable throwable);
}
