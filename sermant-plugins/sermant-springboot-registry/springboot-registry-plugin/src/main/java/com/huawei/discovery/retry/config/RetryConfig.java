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

package com.huawei.discovery.retry.config;

import java.util.function.Function;
import java.util.function.Predicate;

/**
 * 重试配置
 *
 * @author zhouss
 * @since 2022-10-18
 */
public interface RetryConfig {
    /**
     * 重试配置名称
     *
     * @return name
     */
    String getName();

    /**
     * 获取重试等待时间
     *
     * @return 重试等待时间
     */
    long getRetryRetryWaitMs();

    /**
     * 根据重试次数获取重试时间
     *
     * @param retryCount 重试第几次
     * @return Predicate
     */
    Function<Integer, Long> getRetryWaitMs(int retryCount);

    /**
     * 返回最大重试次数
     *
     * @return 最大重试次数
     */
    int getMaxRetry();

    /**
     * 重试异常判断
     *
     * @return Predicate
     */
    Predicate<Throwable> getThrowablePredicate();

    /**
     * 结果重试check
     *
     * @return Predicate
     */
    Predicate<Object> getResultPredicate();
}
