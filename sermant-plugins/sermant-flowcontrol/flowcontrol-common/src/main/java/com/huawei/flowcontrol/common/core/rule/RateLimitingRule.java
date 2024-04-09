/*
 * Copyright (C) 2021-2024 Sermant Authors. All rights reserved.
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

package com.huawei.flowcontrol.common.core.rule;

/**
 * rate limiting rule
 *
 * @author zhouss
 * @since 2021-11-15
 */
public class RateLimitingRule extends AbstractRule {
    /**
     * default timeout
     */
    public static final long DEFAULT_TIMEOUT_DURATION_MS = 0L;

    /**
     * default unit time
     */
    public static final long DEFAULT_LIMIT_REFRESH_PERIOD_MS = 1000L;

    /**
     * default rate number
     */
    public static final int DEFAULT_RATE = 1000;

    /**
     * overtime time
     */
    private String timeoutDuration = "0";

    /**
     * the overtime time after conversion
     */
    private long parsedTimeoutDuration = DEFAULT_TIMEOUT_DURATION_MS;

    /**
     * unit interval
     */
    private String limitRefreshPeriod = "1000";

    /**
     * converted unit time
     */
    private long parsedLimitRefreshPeriod = DEFAULT_LIMIT_REFRESH_PERIOD_MS;

    /**
     * If the rate exceeds the default rate, limiting is triggered
     */
    private int rate = DEFAULT_RATE;

    @Override
    public boolean isInValid() {
        return parsedTimeoutDuration < 0 || parsedLimitRefreshPeriod <= 0 || rate <= 0 || super.isInValid();
    }

    public long getParsedTimeoutDuration() {
        return parsedTimeoutDuration;
    }

    public long getParsedLimitRefreshPeriod() {
        return parsedLimitRefreshPeriod;
    }

    public String getTimeoutDuration() {
        return timeoutDuration;
    }

    /**
     * set the flow control timeout period
     *
     * @param timeoutDuration timeout period
     */
    public void setTimeoutDuration(String timeoutDuration) {
        this.timeoutDuration = timeoutDuration;
        this.parsedTimeoutDuration = parseLongTime(timeoutDuration, DEFAULT_TIMEOUT_DURATION_MS);
    }

    public String getLimitRefreshPeriod() {
        return limitRefreshPeriod;
    }

    /**
     * unit interval
     *
     * @param limitRefreshPeriod unit interval
     */
    public void setLimitRefreshPeriod(String limitRefreshPeriod) {
        this.limitRefreshPeriod = limitRefreshPeriod;
        this.parsedLimitRefreshPeriod = parseLongTime(limitRefreshPeriod, DEFAULT_LIMIT_REFRESH_PERIOD_MS);
    }

    public int getRate() {
        return rate;
    }

    public void setRate(int rate) {
        this.rate = rate;
    }
}
