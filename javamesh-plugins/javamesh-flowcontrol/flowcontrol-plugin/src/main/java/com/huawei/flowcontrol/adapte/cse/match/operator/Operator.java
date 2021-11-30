/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.flowcontrol.adapte.cse.match.operator;

/**
 * 基于{@link com.huawei.flowcontrol.adapte.cse.match.RawOperator} 进行匹配
 *
 * @author zhouss
 * @since 2021-11-22
 */
public interface Operator {
    /**
     * 键值匹配
     *
     * @param targetValue 目标匹配串
     * @param patternValue 匹配匹配串
     * @return 是否匹配成功
     */
    boolean match(String targetValue, String patternValue);

    /**
     * 匹配器ID
     * 用于与线上CSE适配， 例如
     * exact  相等
     * prefix 前缀
     * suffix 后缀
     * contain 包含
     *
     * @return id
     */
    String getId();
}
