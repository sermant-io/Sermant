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

import com.huawei.flowcontrol.common.adapte.cse.rule.RetryRule;
import com.huawei.sermant.core.common.LoggerFactory;

import java.util.Arrays;
import java.util.HashSet;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.logging.Logger;

/**
 * 重试处理器
 *
 * @author zhouss
 * @since 2022-01-26
 */
public class RetryProcessor {
    private static final Logger LOGGER = LoggerFactory.getLogger();

    /**
     * 最大尝试次数
     */
    private final int maxAttempts;

    /**
     * 重试等待时间
     */
    private final Function<Integer, Long> intervalFunction;

    /**
     * 异常比对
     */
    private final Predicate<Throwable> exceptionPredicate;

    /**
     * 结果比对
     */
    private final Predicate<Object> resultPredicate;

    public RetryProcessor(RetryRule retryRule, Retry retry, Function<Integer, Long> intervalFunction) {
        this.maxAttempts = retryRule.getMaxAttempts();
        this.intervalFunction = intervalFunction;
        this.resultPredicate = result -> retry.needRetry(new HashSet<>(retryRule.getRetryOnResponseStatus()), result);
        this.exceptionPredicate = exception -> {
            final Class<? extends Throwable>[] classes = retry.retryExceptions();
            if (classes == null || classes.length == 0) {
                return false;
            }
            return Arrays.stream(classes).anyMatch(clazz -> exception.getClass().isAssignableFrom(clazz));
        };
    }

    @SuppressWarnings("checkstyle:IllegalCatch")
    public Object checkAndRetry(Object result, Supplier<Object> func, Throwable throwable) {
        RetryChecker retryChecker = new RetryChecker();
        Object curResult = result;
        Throwable curException = throwable;
        while (true) {
            final int attemptCount = retryChecker.incrementAndGet();
            try {
                boolean isNeedRetry = false;

                // 存在异常情况, 优先判断异常是否需要重试
                if (curException != null) {
                    isNeedRetry = retryChecker.onThrow(curException, attemptCount);
                    curException = null;
                }
                if (!isNeedRetry) {
                    isNeedRetry = retryChecker.onResult(curResult, attemptCount);
                }
                if (!isNeedRetry) {
                    return curResult;
                }
                LOGGER.fine("Do retry..");
                curResult = func.get();
            } catch (Throwable ex) {
                if (ex instanceof NoClassDefFoundError) {
                    return result;
                }
                curException = ex;
            }
        }
    }

    class RetryChecker {
        /**
         * 已尝试的次数
         */
        private final AtomicInteger attemptCount = new AtomicInteger();

        RetryChecker() {
            this(0);
        }

        RetryChecker(int count) {
            attemptCount.set(count);
        }

        public int incrementAndGet() {
            return attemptCount.incrementAndGet();
        }

        /**
         * 校验结果 决定是否重试
         *
         * @param result 方法调用结果
         * @return true 执行重试， 否则无需重试返回
         */
        public boolean onResult(Object result, int curAttemptCount) {
            if (resultPredicate.test(result)) {
                if (curAttemptCount >= maxAttempts) {
                    return false;
                } else {
                    sleepInterval(curAttemptCount);
                    return true;
                }
            }
            return false;
        }

        /**
         * 通过异常确定是需要进行重试
         *
         * @param throwable 异常信息
         */
        public boolean onThrow(Throwable throwable, int curAttemptCount) {
            if (exceptionPredicate.test(throwable)) {
                if (curAttemptCount < maxAttempts) {
                    sleepInterval(curAttemptCount);
                    return true;
                }
            }
            return false;
        }

        private void sleepInterval(int curAttemptCount) {
            try {
                // 等待一定时间后重试
                final long interval = intervalFunction.apply(curAttemptCount);
                Thread.sleep(interval);
            } catch (InterruptedException ignored) {
                // ignored
            }
        }
    }
}
