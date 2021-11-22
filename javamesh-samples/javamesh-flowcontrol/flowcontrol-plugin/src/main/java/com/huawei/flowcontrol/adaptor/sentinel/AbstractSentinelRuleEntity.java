/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.flowcontrol.adaptor.sentinel;

import com.alibaba.csp.sentinel.slots.block.Rule;

import java.util.UUID;

/**
 * 抽象实现
 *
 * @author zhouss
 * @since 2021-11-22
 */
public abstract class AbstractSentinelRuleEntity<R extends Rule> implements SentinelRuleEntity{

    protected String resource;

    @Override
    public String getResource() {
        if (resource == null) {
            resource = UUID.randomUUID().toString().replace("-", "");
        }
        return resource;
    }

    /**
     * sentinel规则
     *
     * @return Rule
     */
    public abstract R getRule();
}
