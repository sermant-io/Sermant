/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2021. All rights reserved.
 */

package com.huawei.route.common.gray.label.entity;

/**
 * 参数匹配
 *
 * @author pengyuyi
 * @date 2021/10/27
 */
public class MatchRule {
    /**
     * 值匹配规则
     */
    private ValueMatch valueMatch;

    /**
     * 是否区分大小写
     */
    private boolean caseInsensitive;

    /**
     * dubbo获取参数的类型: [留空], [.name], [.isEnabled()], [[0]], [.get(0)], [.get("key")]
     */
    private String type;

    public ValueMatch getValueMatch() {
        return valueMatch;
    }

    public void setValueMatch(ValueMatch valueMatch) {
        this.valueMatch = valueMatch;
    }

    public boolean isCaseInsensitive() {
        return caseInsensitive;
    }

    public void setCaseInsensitive(boolean caseInsensitive) {
        this.caseInsensitive = caseInsensitive;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
