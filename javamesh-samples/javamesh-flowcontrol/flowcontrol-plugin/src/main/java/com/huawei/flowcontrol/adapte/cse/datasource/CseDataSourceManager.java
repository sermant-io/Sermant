/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.flowcontrol.adapte.cse.datasource;

import com.alibaba.csp.sentinel.datasource.Converter;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRuleManager;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import com.huawei.apm.core.lubanops.bootstrap.log.LogFactory;
import com.huawei.flowcontrol.adapte.cse.ResolverManager;
import com.huawei.flowcontrol.adapte.cse.resolver.CircuitBreakerRuleResolver;
import com.huawei.flowcontrol.adapte.cse.resolver.RateLimitingRuleResolver;
import com.huawei.flowcontrol.adapte.cse.resolver.listener.ConfigUpdateListener;
import com.huawei.flowcontrol.adapte.cse.rule.CircuitBreakerRule;
import com.huawei.flowcontrol.adapte.cse.rule.RateLimitingRule;
import com.huawei.flowcontrol.core.datasource.DataSourceManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Logger;

/**
 * 数据源管理
 *
 * @author zhouss
 * @since 2021-11-24
 */
public class CseDataSourceManager implements DataSourceManager {
    private static final Logger LOGGER = LogFactory.getLogger();

    @Override
    public void start() {
        registerFlowRuleDataSource();
        registerBreakerRuleDataSource();
    }

    @Override
    public void stop() {

    }

    /**
     * 注册熔断规则
     */
    private void registerBreakerRuleDataSource() {
        try {
            final CseKieDataSource<CircuitBreakerRule, DegradeRule> degradeRuleDataSource =
                    new CseKieDataSource<CircuitBreakerRule, DegradeRule>(new Converter<List<CircuitBreakerRule>, List<DegradeRule>>() {
                        @Override
                        public List<DegradeRule> convert(List<CircuitBreakerRule> source) {
                            if (source == null) {
                                return null;
                            }
                            final List<DegradeRule> degradeRules = new ArrayList<DegradeRule>();
                            for (CircuitBreakerRule rule : source) {
                                degradeRules.addAll(rule.convertToSentinelRule());
                            }
                            return degradeRules;
                        }
                    });
            DegradeRuleManager.register2Property(degradeRuleDataSource.getProperty());
            final ConfigUpdateListener<CircuitBreakerRule> configUpdateListener = new ConfigUpdateListener<CircuitBreakerRule>() {
                @Override
                public void notify(Map<String, CircuitBreakerRule> rules) {
                    degradeRuleDataSource.updateConfig(new ArrayList<CircuitBreakerRule>(rules.values()));
                }
            };
            ResolverManager.INSTANCE.registerListener(CircuitBreakerRuleResolver.CONFIG_KEY, configUpdateListener);
        } catch (Exception ex) {
            LOGGER.warning(String.format(Locale.ENGLISH, "Init cse kie degrade rule failed! %s", ex.getMessage()));
        }
    }

    /**
     * 注册流控规则
     */
    private void registerFlowRuleDataSource() {
        try {
            final CseKieDataSource<RateLimitingRule, FlowRule> flowRuleDataSource =
                    new CseKieDataSource<RateLimitingRule, FlowRule>(new Converter<List<RateLimitingRule>, List<FlowRule>>() {
                        @Override
                        public List<FlowRule> convert(List<RateLimitingRule> source) {
                            if (source == null) {
                                return null;
                            }
                            final List<FlowRule> flowRules = new ArrayList<FlowRule>();
                            for (RateLimitingRule rule : source) {
                                flowRules.addAll(rule.convertToSentinelRule());
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
        } catch (Exception ex) {
            LOGGER.warning(String.format(Locale.ENGLISH, "Init cse kie flow rule failed! %s", ex.getMessage()));
        }
    }
}
