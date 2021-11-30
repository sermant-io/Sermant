/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2021. All rights reserved.
 */

package com.huawei.flowcontrol.core.datasource.kie.rule.authority;

import com.alibaba.csp.sentinel.datasource.AbstractDataSource;
import com.alibaba.csp.sentinel.property.SentinelProperty;
import com.alibaba.csp.sentinel.slots.block.authority.AuthorityRule;
import com.alibaba.csp.sentinel.slots.block.authority.AuthorityRuleManager;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRuleManager;
import com.huawei.flowcontrol.core.datasource.kie.rule.RuleWrapper;

import java.util.List;

/**
 * 降级规则包装类
 *
 * @author zhouss
 * @since 2021-11-28
 */
public class AuthorityRuleWrapper extends RuleWrapper {
    /**
     * 注册降级规则到降级规则管理器
     *
     * @param dataSource 数据源
     */
    @Override
    public void registerRuleManager(AbstractDataSource<?, ?> dataSource) {
        SentinelProperty property = dataSource.getProperty();
        if (property != null) {
            AuthorityRuleManager.register2Property((SentinelProperty<List<AuthorityRule>>)property);
        }
    }

    /**
     * 获取规则数据的类信息
     *
     * @return class对象
     */
    @Override
    protected Class<?> getRuleClass() {
        return AuthorityRule.class;
    }
}
