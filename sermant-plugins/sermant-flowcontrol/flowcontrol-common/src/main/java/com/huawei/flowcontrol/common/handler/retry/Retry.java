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

package com.huawei.flowcontrol.common.handler.retry;

import java.util.Set;

/**
 * 定义重试
 *
 * @author zhouss
 * @since 2022-01-26
 */
public interface Retry {
    /**
     * 是否需要重试
     *
     * @param statusList 状态码列表， 仅http应用有效
     * @param result 响应结果
     * @return 是否重试
     */
    boolean needRetry(Set<String> statusList, Object result);

    /**
     * 定义哪些异常需要重试
     *
     * @return 需重试的异常
     */
    Class<? extends Throwable>[] retryExceptions();

    /**
     * 重试框架类型
     *
     * @return 框架
     */
    RetryFramework retryType();

    enum RetryFramework {
        /**
         * spring重试
         */
        SPRING_CLOUD,

        /**
         * alibaba dubbo 重试
         */
        ALIBABA_DUBBO,

        /**
         * apache dubbo 重试
         */
        APACHE_DUBBO
    }
}
