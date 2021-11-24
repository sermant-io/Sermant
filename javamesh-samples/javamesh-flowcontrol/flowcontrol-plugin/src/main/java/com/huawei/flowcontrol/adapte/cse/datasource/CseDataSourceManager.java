/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.flowcontrol.adapte.cse.datasource;

import com.alibaba.csp.sentinel.datasource.Converter;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import com.huawei.flowcontrol.adapte.cse.ResolverManager;
import com.huawei.flowcontrol.adapte.cse.resolver.RateLimitingRuleResolver;
import com.huawei.flowcontrol.adapte.cse.resolver.listener.ConfigUpdateListener;
import com.huawei.flowcontrol.adapte.cse.rule.RateLimitingRule;
import com.huawei.flowcontrol.core.datasource.DataSourceManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 数据源管理
 *
 * @author zhouss
 * @since 2021-11-24
 */
public class CseDataSourceManager implements DataSourceManager {
    @Override
    public void initRules() {
        registerFlowRuleDataSource();
    }

    /**
     * 注册流控规则
     */
    private void registerFlowRuleDataSource() {
        final CseKieDataSource<RateLimitingRule, FlowRule> flowRuleDataSource =
                new CseKieDataSource<RateLimitingRule, FlowRule>(new Converter<List<RateLimitingRule>, List<FlowRule>>() {
                    @Override
                    public List<FlowRule> convert(List<RateLimitingRule> source) {
                        if (source == null) {
                            return null;
                        }
                        final List<FlowRule> flowRules = new ArrayList<FlowRule>();
                        for (RateLimitingRule rule : source) {
                            flowRules.add(rule.convertToSentinelRule());
                        }
                        return flowRules;
                    }
                });
        FlowRuleManager.register2Property(flowRuleDataSource.getProperty());
        final ConfigUpdateListener<RateLimitingRule> configUpdateListener = new ConfigUpdateListener<RateLimitingRule>() {

            @Override
            public void notify(Map<String, RateLimitingRule> rules) {
                flowRuleDataSource.updateConfig(new ArrayList<RateLimitingRule>(rules.values()));
            }
        };
        ResolverManager.INSTANCE.registerListener(RateLimitingRuleResolver.CONFIG_KEY, configUpdateListener);
    }
}
