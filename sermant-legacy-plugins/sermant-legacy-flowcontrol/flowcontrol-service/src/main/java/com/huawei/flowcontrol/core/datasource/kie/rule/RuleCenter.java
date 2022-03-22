/*
 * Copyright (C) 2020-2022 Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.huawei.flowcontrol.core.datasource.kie.rule;

import com.huawei.flowcontrol.common.config.CommonConst;
import com.huawei.flowcontrol.core.datasource.kie.rule.authority.AuthorityRuleWrapper;
import com.huawei.flowcontrol.core.datasource.kie.rule.degrade.DegradeRuleWrapper;
import com.huawei.flowcontrol.core.datasource.kie.rule.flow.FlowRuleWrapper;
import com.huawei.flowcontrol.core.datasource.kie.rule.isolate.IsolateThreadRuleWrapper;
import com.huawei.flowcontrol.core.datasource.kie.rule.system.SystemRuleWrapper;

import com.alibaba.csp.sentinel.datasource.AbstractDataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;
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
        ruleMap.put(CommonConst.FLOW_RULE_CONFIG_KEY, new FlowRuleWrapper());
        ruleMap.put(CommonConst.BREAKER_RULE_CONFIG_KEY, new DegradeRuleWrapper());
        ruleMap.put(CommonConst.SYSTEM_RULE_CONFIG_KEY, new SystemRuleWrapper());
        ruleMap.put(CommonConst.AUTHORITY_RULE_CONFIG_KEY, new AuthorityRuleWrapper());
        ruleMap.put(CommonConst.BULK_RULE_CONFIG_KEY, new IsolateThreadRuleWrapper());
    }

    /**
     * 注册规则管理器，将相关规则注册到对应管理器
     *
     * @param ruleType 规则类型
     * @param dataSource 数据源
     */
    public void registerRuleManager(String ruleType, AbstractDataSource<?, ?> dataSource) {
        RuleWrapper ruleWrapper = ruleMap.get(ruleType);
        if (ruleWrapper == null) {
            LOGGER.error(String.format(Locale.ENGLISH, "Un support rule type %s.", ruleType));
            return;
        }
        LOGGER.info(String.format(Locale.ENGLISH, "Register %s to rule manager.", ruleType));
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
