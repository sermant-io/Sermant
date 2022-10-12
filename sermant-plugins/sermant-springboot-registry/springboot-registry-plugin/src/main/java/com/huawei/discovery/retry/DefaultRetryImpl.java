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

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;

/**
 * 默认重试器
 *
 * @author zhouss
 * @since 2022-09-28
 */
public class DefaultRetryImpl implements Retry {
    private final RetryConfig retryConfig;

    private final String name;

    /**
     * 构造器
     *
     * @param name 重拾器名称
     * @param retryConfig 重试配置
     * @throws IllegalArgumentException 参数异常抛出
     */
    public DefaultRetryImpl(RetryConfig retryConfig, String name) {
        if (retryConfig == null || name == null) {
            throw new IllegalArgumentException("retry config or name can not be null!");
        }
        this.retryConfig = retryConfig;
        this.name = name;
    }

    @Override
    public RetryConfig config() {
        return retryConfig;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public <T extends Recorder> RetryContext<T> context() {
        return new RetryContextImpl<>();
    }

    /**
     * 上下文实现
     *
     * @param <T> 记录类型
     * @since 2022-09-28
     */
    class RetryContextImpl<T extends Recorder> implements RetryContext<T> {
        /**
         * 调用次数
         */
        private final AtomicInteger invokeCount = new AtomicInteger();

        @Override
        public void onBefore(Recorder serviceInstanceStats) {
            serviceInstanceStats.beforeRequest();
        }

        @Override
        public boolean onResult(Recorder serviceInstanceStats, Object result, long consumeTimeMs) {
            serviceInstanceStats.afterRequest(consumeTimeMs);
            final Predicate<Object> resultPredicate = config().getResultPredicate();
            if (resultPredicate != null && resultPredicate.test(result)) {
                final int num = invokeCount.incrementAndGet();
                if (num <= config().getMaxRetry()) {
                    waitToRetry();
                    return true;
                }
            }
            return false;
        }

        @Override
        public void onError(Recorder serviceInstanceStats, Exception ex, long consumeTimeMs) throws Exception {
            serviceInstanceStats.errorRequest(ex, consumeTimeMs);
            final Predicate<Throwable> throwablePredicate = config().getThrowablePredicate();
            if (throwablePredicate != null && throwablePredicate.test(ex)) {
                final int num = invokeCount.incrementAndGet();
                if (num <= config().getMaxRetry()) {
                    waitToRetry();
                    return;
                }
            }

            // 抛出异常
            throw new RetryException(ex);
        }

        @Override
        public void onComplete(Recorder serviceInstanceStats) {
            serviceInstanceStats.completeRequest();
        }

        private void waitToRetry() {
            try {
                Thread.sleep(config().getRetryRetryWaitMs());
            } catch (InterruptedException ignored) {
                // 一般情况不会触发
            }
        }
    }
}
