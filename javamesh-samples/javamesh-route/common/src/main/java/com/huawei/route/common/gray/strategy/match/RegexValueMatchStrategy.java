/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2021. All rights reserved.
 */

package com.huawei.route.common.gray.strategy.match;

import com.huawei.route.common.gray.strategy.ValueMatchStrategy;
import com.huawei.route.common.utils.CollectionUtils;

import java.util.List;
import java.util.regex.Pattern;

/**
 * 正则表达式匹配策略
 *
 * @author pengyuyi
 * @date 2021/10/23
 */
public class RegexValueMatchStrategy implements ValueMatchStrategy {
    @Override
    public boolean isMatch(List<String> values, String arg) {
        return !CollectionUtils.isEmpty(values) && values.get(0) != null && arg != null
                && Pattern.matches(values.get(0), arg);
    }
}
