/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2021. All rights reserved.
 */

package com.lubanops.apm.plugin.flowcontrol.core.datasource.kie.rule.system;

import com.alibaba.csp.sentinel.datasource.AbstractDataSource;
import com.alibaba.csp.sentinel.property.SentinelProperty;
import com.alibaba.csp.sentinel.slots.system.SystemRule;
import com.alibaba.csp.sentinel.slots.system.SystemRuleManager;
import com.lubanops.apm.plugin.flowcontrol.core.datasource.kie.rule.RuleWrapper;

import java.util.List;

/**
 * 系统规则包装类
 *
 * @author hanpeng
 * @since 2020-10-14
 */
public class SystemRuleWrapper extends RuleWrapper {
    /**
     * 注册系统规则到系统规则管理器
     *
     * @param dataSource 数据源
     */
    @Override
    public void registerRuleManager(AbstractDataSource<String, ?> dataSource) {
        SentinelProperty property = dataSource.getProperty();
        if (property != null) {
            SystemRuleManager.register2Property((SentinelProperty<List<SystemRule>>) property);
        }
    }

    /**
     * 获取规则数据的类信息
     *
     * @return 返回class对象
     */
    @Override
    protected Class<?> getRuleClass() {
        return SystemRule.class;
    }
}
