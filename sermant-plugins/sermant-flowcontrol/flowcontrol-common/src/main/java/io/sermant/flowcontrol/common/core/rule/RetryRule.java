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

import java.util.ArrayList;
import java.util.List;

/**
 * retry rule
 *
 * @author zhouss
 * @since 2021-11-15
 */
public class RetryRule extends AbstractRule {
    /**
     * retry strategy
     */
    public static final String STRATEGY_RANDOM_BACKOFF = "RandomBackoff";

    /**
     * default maximum number of attempts
     */
    public static final int DEFAULT_MAX_ATTEMPTS = 3;

    /**
     * default wait time for next request
     */
    public static final long DEFAULT_WAIT_DURATION_MS = 10L;

    /**
     * default retry status code
     */
    public static final String DEFAULT_RETRY_ON_RESPONSE_STATUS = "502";

    /**
     * default initial base
     */
    private static final long DEFAULT_INITIAL_INTERVAL_MS = 1000L;

    /**
     * default index
     */
    private static final float DEFAULT_MULTIPLIER = 2f;

    /**
     * default random factor
     */
    private static final double DEFAULT_RANDOMIZATION_FACTOR = 0.5d;

    /**
     * default retry policy
     */
    private static final String DEFAULT_RETRY_STRATEGY = "FixedInterval";

    /**
     * Minimum reference time
     */
    private static final long MIN_INITIAL_INTERVAL_MS = 10L;

    /**
     * maximum attempts
     */
    private int maxAttempts = DEFAULT_MAX_ATTEMPTS;

    /**
     * time to wait for each retry attempt。
     */
    private String waitDuration = String.valueOf(DEFAULT_WAIT_DURATION_MS);

    /**
     * the attempt wait time after conversion
     */
    private long parsedWaitDuration = DEFAULT_WAIT_DURATION_MS;

    /**
     * http status that needs to be retried, separated by commas
     */
    private List<String> retryOnResponseStatus = new ArrayList<>();

    /**
     * retry strategy
     */
    private String retryStrategy = DEFAULT_RETRY_STRATEGY;

    /**
     * reference time
     */
    private String initialInterval = String.valueOf(DEFAULT_INITIAL_INTERVAL_MS);

    /**
     * the converted base time
     */
    private long parsedInitialInterval = DEFAULT_INITIAL_INTERVAL_MS;

    /**
     * exponential basis
     */
    private float multiplier = DEFAULT_MULTIPLIER;

    /**
     * random factor
     */
    private double randomizationFactor = DEFAULT_RANDOMIZATION_FACTOR;

    /**
     * failed after maximum retry
     */
    private boolean failAfterMaxAttempts = false;

    /**
     * Whether to try again on the same machine, this configuration is used for load balancing,
     * and the current agent-based interceptor is used instead
     */
    private int retryOnSame = 0;

    @Override
    public boolean isInValid() {
        if (maxAttempts < 1) {
            return true;
        }
        if (retryOnSame < 0) {
            return true;
        }
        if (parsedWaitDuration < 0) {
            return true;
        }
        if (parsedInitialInterval < MIN_INITIAL_INTERVAL_MS) {
            return true;
        }
        return super.isInValid();
    }

    public boolean isFailAfterMaxAttempts() {
        return failAfterMaxAttempts;
    }

    public void setFailAfterMaxAttempts(boolean failAfterMaxAttempts) {
        this.failAfterMaxAttempts = failAfterMaxAttempts;
    }

    public int getMaxAttempts() {
        return maxAttempts;
    }

    public void setMaxAttempts(int maxAttempts) {
        this.maxAttempts = maxAttempts;
    }

    public String getWaitDuration() {
        return waitDuration;
    }

    public int getRetryOnSame() {
        return retryOnSame;
    }

    public void setRetryOnSame(int retryOnSame) {
        this.retryOnSame = retryOnSame;
    }

    /**
     * retry waiting interval
     *
     * @param waitDuration waiting interval
     */
    public void setWaitDuration(String waitDuration) {
        this.waitDuration = waitDuration;
        this.parsedWaitDuration = parseLongTime(waitDuration, DEFAULT_WAIT_DURATION_MS);
    }

    public List<String> getRetryOnResponseStatus() {
        return retryOnResponseStatus;
    }

    public void setRetryOnResponseStatus(List<String> retryOnResponseStatus) {
        this.retryOnResponseStatus = retryOnResponseStatus;
    }

    public String getRetryStrategy() {
        return retryStrategy;
    }

    public void setRetryStrategy(String retryStrategy) {
        this.retryStrategy = retryStrategy;
    }

    public String getInitialInterval() {
        return initialInterval;
    }

    /**
     * base
     *
     * @param initialInterval base
     */
    public void setInitialInterval(String initialInterval) {
        this.initialInterval = initialInterval;
        this.parsedInitialInterval = parseLongTime(initialInterval, DEFAULT_INITIAL_INTERVAL_MS);
    }

    public float getMultiplier() {
        return multiplier;
    }

    public void setMultiplier(float multiplier) {
        this.multiplier = multiplier;
    }

    public double getRandomizationFactor() {
        return randomizationFactor;
    }

    public void setRandomizationFactor(double randomizationFactor) {
        this.randomizationFactor = randomizationFactor;
    }

    public long getParsedWaitDuration() {
        return parsedWaitDuration;
    }

    public long getParsedInitialInterval() {
        return parsedInitialInterval;
    }
}
