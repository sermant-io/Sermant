/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2021. All rights reserved.
 */

package com.huawei.flowcontrol.core.datasource.kie.rule.flow;

import com.alibaba.csp.sentinel.datasource.AbstractDataSource;
import com.alibaba.csp.sentinel.property.SentinelProperty;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import com.huawei.flowcontrol.core.datasource.kie.rule.RuleWrapper;

import java.util.List;

/**
 * 流控规则包装类
 *
 * @author hanpeng
 * @since 2020-10-14
 */
public class FlowRuleWrapper extends RuleWrapper {
    /**
     * 注册流控规则到流控规则管理器
     *
     * @param dataSource 数据源
     */
    @Override
    public void registerRuleManager(AbstractDataSource<String, ?> dataSource) {
        SentinelProperty property = dataSource.getProperty();
        if (property != null) {
            FlowRuleManager.register2Property((SentinelProperty<List<FlowRule>>) property);
        }
    }

    /**
     * 获取规则数据的类信息
     *
     * @return 返回class对象
     */
    @Override
    protected Class<?> getRuleClass() {
        return FlowRule.class;
    }
}
