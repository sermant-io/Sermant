/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.flowcontrol.adapte.cse.match.operator;

import com.huawei.flowcontrol.util.StringUtils;

/**
 * 后缀匹配
 *
 * @author zhouss
 * @since 2021-11-22
 */
public class SuffixOperator implements Operator {
    @Override
    public boolean match(String targetValue, String patternValue) {
        return StringUtils.suffix(targetValue, patternValue);
    }

    @Override
    public String getId() {
        return "suffix";
    }
}
