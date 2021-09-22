/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2021. All rights reserved.
 */

package com.lubanops.apm.plugin.flowcontrol.core.datasource.kie.rule;

import com.alibaba.csp.sentinel.datasource.AbstractDataSource;

/**
 * 规则包装类的抽象类
 *
 * @author hanpeng
 * @since 2020-10-14
 */
public abstract class RuleWrapper {
    /**
     * 抽象方法，注册规则数据信息到RuleManager
     *
     * @param dataSource 数据源
     */
    protected abstract void registerRuleManager(AbstractDataSource<String, ?> dataSource);

    /**
     * 抽象方法，获取规则数据的类信息
     *
     * @return 返回class信息
     */
    protected abstract Class<?> getRuleClass();
}
