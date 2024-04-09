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

package com.huawei.flowcontrol.common.core.rule;

import com.huawei.flowcontrol.common.util.StringUtils;

import com.huaweicloud.sermant.core.common.LoggerFactory;

import java.time.DateTimeException;
import java.time.Duration;
import java.util.Locale;
import java.util.Optional;

/**
 * abstract rule
 *
 * @author zhouss
 * @since 2021-11-15
 */
public abstract class AbstractRule extends Configurable implements Rule, Comparable<AbstractRule> {
    /**
     * time format
     */
    private static final String DIGIT_MATCH = "-?[0-9]{1,10}";

    /**
     * duration translation prefix
     */
    private static final String DURATION_PREFIX = "PT";

    /**
     * Rule priority. A smaller rule has a higher priority
     */
    protected int order = 0;

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public int compareTo(AbstractRule target) {
        return order - target.order;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    @Override
    public boolean isInValid() {
        return StringUtils.isEmpty(name);
    }

    /**
     * the string is converted to long milliseconds
     *
     * @param time timeString 1000->1000milliseconds -1000->-1000milliseconds 1000S(s) 1000seconds->1000000milliseconds
     * @param defaultValue default
     * @return 转换值
     * @throws IllegalArgumentException thrown if the argument is not valid
     */
    protected long parseLongTime(String time, long defaultValue) {
        if (StringUtils.isEmpty(time)) {
            return defaultValue;
        }
        if (time.matches(DIGIT_MATCH)) {
            // 没有单位 纯数字
            long result = Long.parseLong(time);
            if (result < 0) {
                throw new IllegalArgumentException("The value of time must ge zero!");
            }
            return result;
        }
        return tryParseWithDuration(time).map(Duration::toMillis).orElse(defaultValue);
    }

    /**
     * duration format conversion
     *
     * @param time raw format
     * @return duration
     */
    private Optional<Duration> tryParseWithDuration(String time) {
        try {
            return Optional.of(Duration.parse(DURATION_PREFIX + time));
        } catch (DateTimeException ex) {
            LoggerFactory.getLogger().warning(String.format(Locale.ENGLISH,
                    "Parsed time [%s] to duration failed!", time));
        }
        return Optional.empty();
    }
}
