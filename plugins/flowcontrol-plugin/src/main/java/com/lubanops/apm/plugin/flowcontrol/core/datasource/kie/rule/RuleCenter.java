/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2021. All rights reserved.
 */

package com.lubanops.apm.plugin.flowcontrol.core.datasource.kie.rule;

import com.alibaba.csp.sentinel.datasource.AbstractDataSource;
import com.lubanops.apm.plugin.flowcontrol.core.datasource.kie.rule.degrade.DegradeRuleWrapper;
import com.lubanops.apm.plugin.flowcontrol.core.datasource.kie.rule.flow.FlowRuleWrapper;
import com.lubanops.apm.plugin.flowcontrol.core.datasource.kie.rule.system.SystemRuleWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 规则中心类
 *
 * @author hanpeng
 * @since 2020-10-14
 */
public class RuleCenter {
    private static final Logger LOGGER = LoggerFactory.getLogger(RuleCenter.class);

    /**
     * 规则Map集合  key-value：规则名称-规则包装类
     */
    private final Map<String, RuleWrapper> ruleMap = new ConcurrentHashMap<String, RuleWrapper>();

    public RuleCenter() {
        ruleMap.put("FlowRule", new FlowRuleWrapper());
        ruleMap.put("DegradeRule", new DegradeRuleWrapper());
        ruleMap.put("SystemRule", new SystemRuleWrapper());
    }

    /**
     * 注册规则管理器，将相关规则注册到对应管理器
     *
     * @param ruleType   规则类型
     * @param dataSource 数据源
     */
    public void registerRuleManager(String ruleType, AbstractDataSource<String, ?> dataSource) {
        if (!ruleMap.containsKey(ruleType)) {
            LOGGER.error(String.format("Un support rule type %s.", ruleType));
            return;
        }
        LOGGER.info(String.format("Register %s to rule manager.", ruleType));
        RuleWrapper ruleWrapper = ruleMap.get(ruleType);
        ruleWrapper.registerRuleManager(dataSource);
    }

    /**
     * 查询规则数据类型
     *
     * @return 返回规则类型set集合
     */
    public Set<String> getRuleTypes() {
        return ruleMap.keySet();
    }

    /**
     * 获取规则数据的类信息
     *
     * @param ruleType 规则类型
     * @return 返回class信息
     */
    public Class<?> getRuleClass(String ruleType) {
        RuleWrapper ruleWrapper = ruleMap.get(ruleType);
        return ruleWrapper.getRuleClass();
    }
}
