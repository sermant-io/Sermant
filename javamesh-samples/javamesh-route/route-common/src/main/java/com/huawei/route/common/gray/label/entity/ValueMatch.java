/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2021. All rights reserved.
 */

package com.huawei.route.common.gray.label.entity;

import java.util.List;

/**
 * 值匹配
 *
 * @author pengyuyi
 * @date 2021/10/28
 */
public class ValueMatch {
    /**
     * 值匹配策略
     */
    private MatchStrategy matchStrategy;

    /**
     * 期望值
     */
    private List<String> values;

    public MatchStrategy getMatchStrategy() {
        return matchStrategy;
    }

    public void setMatchStrategy(MatchStrategy matchStrategy) {
        this.matchStrategy = matchStrategy;
    }

    public List<String> getValues() {
        return values;
    }

    public void setValues(List<String> values) {
        this.values = values;
    }
}
