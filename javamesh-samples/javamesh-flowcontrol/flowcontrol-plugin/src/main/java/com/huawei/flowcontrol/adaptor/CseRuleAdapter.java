/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.flowcontrol.adaptor;

/**
 * CSE规则转换适配器
 *
 * @param <RULE> 规则
 * @param <MATCHER> 匹配器
 * @param <RESULT> 最终结果
 * @author zhouss
 * @since 2021-11-22
 */
public interface CseRuleAdapter<RULE, MATCHER, RESULT> {
    /**
     * CSE规则转换
     *
     * @param rule cse原生规则
     * @param matcher cse匹配器
     * @return 转换的结果
     */
    RESULT adapt(RULE rule, MATCHER matcher);
}
