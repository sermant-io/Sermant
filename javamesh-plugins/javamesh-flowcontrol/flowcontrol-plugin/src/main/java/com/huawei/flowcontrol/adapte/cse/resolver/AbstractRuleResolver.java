/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.flowcontrol.adapte.cse.resolver;

import com.huawei.flowcontrol.adapte.cse.rule.Configurable;

/**
 * 规则解析器
 *
 * @author zhouss
 * @since 2021-11-16
 */
public abstract class AbstractRuleResolver<T extends Configurable> extends AbstractResolver<T> {
    protected AbstractRuleResolver(String configKey) {
        super(configKey);
    }
}
