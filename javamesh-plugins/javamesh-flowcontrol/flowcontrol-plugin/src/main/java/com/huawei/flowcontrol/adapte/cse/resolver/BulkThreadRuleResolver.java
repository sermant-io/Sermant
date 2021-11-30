/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.flowcontrol.adapte.cse.resolver;

import com.huawei.flowcontrol.adapte.cse.rule.BulkThreadRule;

/**
 * 隔离仓配置解析
 *
 * @author zhouss
 * @since 2021-11-16
 */
public class BulkThreadRuleResolver extends AbstractRuleResolver<BulkThreadRule> {
    /**
     * 隔离仓配置 键
     */
    public static final String CONFIG_KEY = "servicecomb.bulkhead";

    public BulkThreadRuleResolver() {
        super(CONFIG_KEY);
    }

    @Override
    protected Class<BulkThreadRule> getRuleClass() {
        return BulkThreadRule.class;
    }
}
