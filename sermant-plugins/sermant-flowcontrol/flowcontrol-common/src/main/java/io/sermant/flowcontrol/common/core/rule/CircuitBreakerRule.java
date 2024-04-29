/*
 * Copyright (C) 2021-2022 Huawei Technologies Co., Ltd. All rights reserved.
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

package io.sermant.flowcontrol.common.core.rule;

import io.sermant.flowcontrol.common.util.StringUtils;

/**
 * Circuit Breaker Rule
 *
 * @author zhouss
 * @since 2021-11-15
 */
public class CircuitBreakerRule extends AbstractRule {
    /**
     * time window type based on time statistics
     */
    public static final String SLIDE_WINDOW_TIME_TYPE = "time";

    /**
     * The type of time window based on the number of requests
     */
    public static final String SLIDE_WINDOW_COUNT_TYPE = "count";

    /**
     * default failure error rate threshold
     */
    public static final float DEFAULT_FAILURE_RATE_THRESHOLD = 50f;

    /**
     * default slow call threshold
     */
    public static final float DEFAULT_SLOW_CALL_RATE_THRESHOLD = 100f;

    /**
     * default Circuit Breaker interval 60s
     */
    public static final long DEFAULT_WAIT_DURATION_IN_OPEN_STATUS_MS = 60000L;

    /**
     * default threshold for slow calls 60S
     */
    public static final long DEFAULT_SLOW_CALL_DURATION_THRESHOLD_MS = 60000L;

    /**
     * The number of requests allowed through the half-open state
     */
    public static final int DEFAULT_PERMITTED = 10;

    /**
     * default minimum number of calls
     */
    public static final int DEFAULT_MINIMUM_NUMBER_CALLS = 100;

    /**
     * The default window size supports time and number of requests
     */
    public static final long DEFAULT_SLIDING_WINDOW_SIZE = 100L;

    /**
     * maximum ratio
     */
    private static final float MAX_PERCENT = 100.0f;

    /**
     * minimum proportion
     */
    private static final float MIN_PERCENT = 0.0f;

    /**
     * Error rate, reaching the error rate triggers the circuit breaker
     */
    private float failureRateThreshold = DEFAULT_FAILURE_RATE_THRESHOLD;

    /**
     * slow call rate
     */
    private float slowCallRateThreshold = DEFAULT_SLOW_CALL_RATE_THRESHOLD;

    /**
     * try requesting interval after circuit breaker
     */
    private String waitDurationInOpenState = String.valueOf(DEFAULT_WAIT_DURATION_IN_OPEN_STATUS_MS);

    /**
     * the interval after conversion
     */
    private long parsedWaitDurationInOpenState = DEFAULT_WAIT_DURATION_IN_OPEN_STATUS_MS;

    /**
     * slow call request interval after circuit breaker
     */
    private String slowCallDurationThreshold = String.valueOf(DEFAULT_SLOW_CALL_DURATION_THRESHOLD_MS);

    /**
     * Slow call after conversion request interval after circuit breaker
     */
    private long parsedSlowCallDurationThreshold = DEFAULT_SLOW_CALL_DURATION_THRESHOLD_MS;

    /**
     * number of half open requests
     */
    private int permittedNumberOfCallsInHalfOpenState = DEFAULT_PERMITTED;

    /**
     * minimum call request base
     */
    private int minimumNumberOfCalls = DEFAULT_MINIMUM_NUMBER_CALLS;

    /**
     * moving window type: count and time
     */
    private String slidingWindowType;

    /**
     * sliding window size
     */
    private String slidingWindowSize = String.valueOf(DEFAULT_SLIDING_WINDOW_SIZE);

    /**
     * converted sliding window size
     */
    private long parsedSlidingWindowSize = DEFAULT_SLIDING_WINDOW_SIZE;

    /**
     * force off the circuit breaker
     */
    private boolean forceClosed = false;

    /**
     * force open the circuit breaker
     */
    private boolean forceOpen = false;

    public boolean isForceClosed() {
        return forceClosed;
    }

    public void setForceClosed(boolean forceClosed) {
        this.forceClosed = forceClosed;
    }

    public boolean isForceOpen() {
        return forceOpen;
    }

    public void setForceOpen(boolean forceOpen) {
        this.forceOpen = forceOpen;
    }

    @Override
    public boolean isInValid() {
        if (failureRateThreshold > MAX_PERCENT || failureRateThreshold <= MIN_PERCENT) {
            return false;
        }
        if (slowCallRateThreshold > MAX_PERCENT || slowCallRateThreshold <= MIN_PERCENT) {
            return false;
        }
        if (parsedWaitDurationInOpenState <= 0 || parsedSlowCallDurationThreshold <= 0) {
            return false;
        }
        if (permittedNumberOfCallsInHalfOpenState <= 0) {
            return false;
        }
        if (minimumNumberOfCalls <= 0) {
            return false;
        }

        return super.isInValid();
    }

    public float getFailureRateThreshold() {
        return failureRateThreshold;
    }

    public void setFailureRateThreshold(float failureRateThreshold) {
        this.failureRateThreshold = failureRateThreshold;
    }

    public float getSlowCallRateThreshold() {
        return slowCallRateThreshold;
    }

    public void setSlowCallRateThreshold(float slowCallRateThreshold) {
        this.slowCallRateThreshold = slowCallRateThreshold;
    }

    public String getWaitDurationInOpenState() {
        return waitDurationInOpenState;
    }

    /**
     * 设置熔断等待时长
     *
     * @param waitDurationInOpenState 等待时长，该格式是duration格式
     */
    public void setWaitDurationInOpenState(String waitDurationInOpenState) {
        this.waitDurationInOpenState = waitDurationInOpenState;
        this.parsedWaitDurationInOpenState = parseLongTime(waitDurationInOpenState,
            DEFAULT_WAIT_DURATION_IN_OPEN_STATUS_MS);
    }

    public String getSlowCallDurationThreshold() {
        return slowCallDurationThreshold;
    }

    /**
     * 设置慢调用阈值
     *
     * @param slowCallDurationThreshold 慢调用阈值
     */
    public void setSlowCallDurationThreshold(String slowCallDurationThreshold) {
        this.slowCallDurationThreshold = slowCallDurationThreshold;
        this.parsedSlowCallDurationThreshold = parseLongTime(slowCallDurationThreshold,
            DEFAULT_SLOW_CALL_DURATION_THRESHOLD_MS);
    }

    public int getPermittedNumberOfCallsInHalfOpenState() {
        return permittedNumberOfCallsInHalfOpenState;
    }

    public void setPermittedNumberOfCallsInHalfOpenState(int permittedNumberOfCallsInHalfOpenState) {
        this.permittedNumberOfCallsInHalfOpenState = permittedNumberOfCallsInHalfOpenState;
    }

    public int getMinimumNumberOfCalls() {
        return minimumNumberOfCalls;
    }

    public void setMinimumNumberOfCalls(int minimumNumberOfCalls) {
        this.minimumNumberOfCalls = minimumNumberOfCalls;
    }

    public String getSlidingWindowType() {
        return slidingWindowType;
    }

    public void setSlidingWindowType(String slidingWindowType) {
        this.slidingWindowType = slidingWindowType;
    }

    public String getSlidingWindowSize() {
        return slidingWindowSize;
    }

    /**
     * 设置时间窗口
     *
     * @param slidingWindowSize 时间窗口值
     */
    public void setSlidingWindowSize(String slidingWindowSize) {
        if (!StringUtils.isEmpty(slidingWindowSize)) {
            this.slidingWindowSize = slidingWindowSize;
            this.parsedSlidingWindowSize = parseLongTime(slidingWindowSize, DEFAULT_SLIDING_WINDOW_SIZE);
        }
    }

    public long getParsedWaitDurationInOpenState() {
        return parsedWaitDurationInOpenState;
    }

    public long getParsedSlowCallDurationThreshold() {
        return parsedSlowCallDurationThreshold;
    }

    public long getParsedSlidingWindowSize() {
        return parsedSlidingWindowSize;
    }
}
