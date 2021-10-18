/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2021. All rights reserved.
 */

package com.lubanops.apm.plugin.flowcontrol.core.datasource.kie.rule.degrade;

import com.alibaba.csp.sentinel.datasource.AbstractDataSource;
import com.alibaba.csp.sentinel.property.SentinelProperty;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRuleManager;
import com.lubanops.apm.plugin.flowcontrol.core.datasource.kie.rule.RuleWrapper;

import java.util.List;

/**
 * 降级规则包装类
 *
 * @author hanpeng
 * @since 2020-10-14
 */
public class DegradeRuleWrapper extends RuleWrapper {
    /**
     * 注册降级规则到降级规则管理器
     *
     * @param dataSource 数据源
     */
    @Override
    public void registerRuleManager(AbstractDataSource<String, ?> dataSource) {
        SentinelProperty property = dataSource.getProperty();
        if (property != null) {
            DegradeRuleManager.register2Property((SentinelProperty<List<DegradeRule>>)property);
        }
    }

    /**
     * 获取规则数据的类信息
     *
     * @return class对象
     */
    @Override
    protected Class<?> getRuleClass() {
        return DegradeRule.class;
    }
}
