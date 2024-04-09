/*
 * Copyright (C) 2022-2024 Sermant Authors. All rights reserved.
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

package com.huawei.flowcontrol.res4j.exceptions;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;

/**
 * instance isolation exception
 *
 * @author zhouss
 * @since 2022-07-22
 */
public class InstanceIsolationException extends CircuitBreakerException {
    /**
     * instance isolation exception
     *
     * @param circuitBreaker circuit breaker
     * @param message circuit breaker information
     * @param writableStackTrace stack or not
     */
    public InstanceIsolationException(CircuitBreaker circuitBreaker,
            String message, boolean writableStackTrace) {
        super(circuitBreaker, message, writableStackTrace);
    }

    /**
     * construct exception message
     *
     * @param circuitBreaker circuit breaker configuration
     * @return exception
     */
    public static CircuitBreakerException createException(CircuitBreaker circuitBreaker) {
        return new InstanceIsolationException(circuitBreaker, createMsg(circuitBreaker),
                circuitBreaker.getCircuitBreakerConfig().isWritableStackTraceEnabled());
    }
}
