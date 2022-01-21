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

import com.huawei.flowcontrol.common.config.CommonConst;
import com.huawei.flowcontrol.common.util.StringUtils;

/**
 * 抽象规则
 *
 * @author zhouss
 * @since 2021-11-15
 */
public abstract class AbstractRule extends Configurable implements Rule {
    /**
     * 时间格式
     */
    private static final String DIGIT_MATCH = "-?[0-9]{1,10}";

    /**
     * 支持的时间单位
     */
    private static final char[] TIME_UNITS = {'s', 'S', 'm', 'M'};

    @Override
    public boolean isValid() {
        return StringUtils.isEmpty(name);
    }

    /**
     * 字符串转long类型毫秒
     *
     * @param time 时间字符串 1000   -> 1000毫秒 -1000  -> 负1000毫秒 1000S(s) 1000秒   -> 1000000毫秒
     * @param defaultValue 默认值
     * @return 转换值
     */
    protected long parseLongTime(String time, long defaultValue) {
        if (StringUtils.isEmpty(time)) {
            return defaultValue;
        }
        if (time.matches(DIGIT_MATCH)) {
            // 没有单位纯数字
            long result = Long.parseLong(time);
            if (result < 0) {
                throw new IllegalArgumentException("The value of time must ge zero!");
            }
            return result;
        }
        return parseWithUnit(time);
    }

    private long parseWithUnit(String time) {
        char unit = time.charAt(time.length() - 1);
        if (!isValidUnit(unit)) {
            throw new IllegalArgumentException("The format of time is invalid!");
        }
        long result;
        try {
            result = Long.parseLong(time.substring(0, time.length() - 1));
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("The format of time is invalid!");
        }
        switch (unit) {
            case 's':
            case 'S':
                return result * CommonConst.S_MS_UNIT;
            case 'm':
            case 'M':
                return result * CommonConst.S_MS_UNIT * CommonConst.S_M_UNIT;
            default:
                throw new IllegalArgumentException("The unit of time is not supported!");
        }
    }

    private boolean isValidUnit(char unit) {
        for (char ch : TIME_UNITS) {
            if (ch == unit) {
                return true;
            }
        }
        return false;
    }
}
