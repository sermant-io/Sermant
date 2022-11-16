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
 * 重试器
 *
 * @author zhouss
 * @since 2022-09-28
 */
public interface Retry {
    /**
     * 创建重试器
     *
     * @param retryConfig 重试配置
     * @return Retry
     */
    static Retry create(RetryConfig retryConfig) {
        return new DefaultRetryImpl(retryConfig, retryConfig.getName());
    }

    /**
     * 获取重试配置
     *
     * @return 重试
     */
    RetryConfig config();

    /**
     * 配置名
     *
     * @return 重试器名称
     */
    String name();

    /**
     * 创建上下文
     *
     * @param <T> 记录的类型
     * @return 重试上下文
     */
    <T extends Recorder> RetryContext<T> context();

    /**
     * 重试上下文
     *
     * @param <T> 记录器类型
     * @since 2022-09-28
     */
    interface RetryContext<T extends Recorder> {
        /**
         * 调用前置
         *
         * @param serviceInstanceStats 选择调用的实例
         */
        void onBefore(T serviceInstanceStats);

        /**
         * 调用结果验证
         *
         * @param serviceInstanceStats 选择调用的实例
         * @param result 调用结果
         * @param consumeTimeMs 调用的消耗时间
         * @return true则表示重试通过或者达到最大重试次数, false则需要进行下一次重试
         */
        boolean onResult(T serviceInstanceStats, Object result, long consumeTimeMs);

        /**
         * 调用异常验证
         *
         * @param serviceInstanceStats 选择调用的实例
         * @param ex 调用异常时调用
         * @param consumeTimeMs 调用的消耗时间
         * @throws Exception 不满足异常重试条件时抛出异常
         */
        void onError(T serviceInstanceStats, Throwable ex, long consumeTimeMs) throws Exception;

        /**
         * 最终结束, 在重试彻底结束后调用该方法
         *
         * @param serviceInstanceStats 选择实例
         */
        void onComplete(T serviceInstanceStats);
    }
}
