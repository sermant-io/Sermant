/*
 * Copyright (C) 2021-2022 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huawei.flowcontrol.common.adapte.cse.rule;

/**
 * 限流规则
 *
 * @author zhouss
 * @since 2021-11-15
 */
public class RateLimitingRule extends AbstractRule {
    /**
     * 默认超时时间
     */
    public static final long DEFAULT_TIMEOUT_DURATION_MS = 0L;

    /**
     * 默认单位时间
     */
    public static final long DEFAULT_LIMIT_REFRESH_PERIOD_MS = 1000L;

    /**
     * 默认许可数
     */
    public static final int DEFAULT_RATE = 1000;

    /**
     * 超时时间
     */
    private String timeoutDuration = "0";

    /**
     * 转换后的超时时间
     */
    private long parsedTimeoutDuration = DEFAULT_TIMEOUT_DURATION_MS;

    /**
     * 单位时间
     */
    private String limitRefreshPeriod = "1000";

    /**
     * 转换后的单位时间
     */
    private long parsedLimitRefreshPeriod = DEFAULT_LIMIT_REFRESH_PERIOD_MS;

    /**
     * 默认许可数 单位时间内超过该许可数便会触发限流
     */
    private int rate = DEFAULT_RATE;

    @Override
    public boolean isValid() {
        return parsedTimeoutDuration < 0 || parsedLimitRefreshPeriod <= 0 || rate <= 0 || super.isValid();
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

    public void setTimeoutDuration(String timeoutDuration) {
        this.timeoutDuration = timeoutDuration;
        this.parsedTimeoutDuration = parseLongTime(timeoutDuration, DEFAULT_TIMEOUT_DURATION_MS);
    }

    public String getLimitRefreshPeriod() {
        return limitRefreshPeriod;
    }

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
