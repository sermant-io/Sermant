/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2021. All rights reserved.
 */

package com.huawei.route.common.gray.strategy.match;

import com.huawei.route.common.gray.strategy.ValueMatchStrategy;

import java.util.List;

/**
 * 不小于匹配策略
 *
 * @author pengyuyi
 * @date 2021/10/23
 */
public class NoLessValueMatchStrategy implements ValueMatchStrategy {
    @Override
    public boolean isMatch(List<String> values, String arg) {
        try {
            return Integer.parseInt(arg) >= Integer.parseInt(values.get(0));
        } catch (Exception e) {
            return false;
        }
    }
}
