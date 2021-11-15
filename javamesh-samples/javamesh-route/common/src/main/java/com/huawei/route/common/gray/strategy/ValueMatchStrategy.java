/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2021. All rights reserved.
 */

package com.huawei.route.common.gray.strategy;

import java.util.List;

/**
 * 值匹配策略
 *
 * @author pengyuyi
 * @date 2021/10/14
 */
public interface ValueMatchStrategy {
    /**
     * 是否匹配
     *
     * @param values 期望值
     * @param arg 参数值
     * @return 是否匹配
     */
    boolean isMatch(List<String> values, String arg);
}
