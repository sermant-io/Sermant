/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.route.server.rules.enums;

import org.apache.commons.lang3.StringUtils;

/**
 * 规则类型
 *
 * @author zhouss
 * @since 2021-10-12
 */
public enum RuleTypeEnum {
    /**
     * 权重规则
     */
    WEIGHT,

    /**
     * 按照标签内容匹配
     */
    CONTENT,

    /**
     * 全局规则
     */
    GLOBAL;

    /**
     * 该规则类型是否合法
     *
     * @param ruleType 规则类型
     * @return 是否合法
     */
    public static boolean isValid(String ruleType) {
        if (StringUtils.isEmpty(ruleType)) {
            return false;
        }
        for (RuleTypeEnum ruleTypeEnum : RuleTypeEnum.values()) {
            if (StringUtils.equals(ruleTypeEnum.name(), ruleType)) {
                return true;
            }
        }
        return false;
    }
}
