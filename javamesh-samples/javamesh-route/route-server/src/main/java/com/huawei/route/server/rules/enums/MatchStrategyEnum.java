/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.route.server.rules.enums;

/**
 * 参数匹配策略
 *
 * @author zhouss
 * @since 2021-10-13
 */
public enum MatchStrategyEnum {
    /**
     * 相等
     */
    EQUAL,

    /**
     * 前缀
     */
    PREFIX,

    /**
     * 在集合中
     */
    IN
}
