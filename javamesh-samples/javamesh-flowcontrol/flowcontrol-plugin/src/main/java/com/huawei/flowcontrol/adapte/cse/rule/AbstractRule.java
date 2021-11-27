/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.flowcontrol.adapte.cse.rule;

import com.huawei.flowcontrol.util.StringUtils;

import java.util.List;

/**
 * 抽象规则
 *
 * @author zhouss
 * @since 2021-11-15
 */
public abstract class AbstractRule<SENTINEL_RULE extends com.alibaba.csp.sentinel.slots.block.Rule> extends Configurable implements Rule {

    private String resource;

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
     * 转换为sentinel规则
     *
     * @return sentinel规则
     */
    public abstract List<SENTINEL_RULE> convertToSentinelRule();

    /**
     * 字符串转long类型毫秒
     *
     * @param time 时间字符串
     *             1000   -> 1000毫秒
     *             -1000  -> 负1000毫秒
     *             1000S(s) 1000秒   -> 1000000毫秒
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
            case 's' :
            case 'S' :
                return result * 1000;
            case 'm' :
            case 'M' :
                return result * 1000 * 60;
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
