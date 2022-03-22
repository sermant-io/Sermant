/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/*
 * Based on com/alibaba/csp/sentinel/slots/block/degrade/DegradeRuleManager.java
 * from the Alibaba Sentinel project.
 */

package com.huawei.flowcontrol.adapte.cse.rule.isolate;

import com.alibaba.csp.sentinel.property.DynamicSentinelProperty;
import com.alibaba.csp.sentinel.property.PropertyListener;
import com.alibaba.csp.sentinel.property.SentinelProperty;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 隔离仓规则管理
 *
 * @author zhouss
 * @since 2021-12-04
 */
public class IsolateThreadRuleManager {
    /**
     * 规则缓存 该规则一个资源仅有一个规则，相同则覆盖
     */
    private static final Map<String, List<IsolateThreadRule>> BULK_THREAD_RULE_CACHE =
        new HashMap<String, List<IsolateThreadRule>>();

    /**
     * 动态规则
     */
    private static SentinelProperty<List<IsolateThreadRule>> ruleProperty =
        new DynamicSentinelProperty<List<IsolateThreadRule>>();

    /**
     * 规则监听器
     */
    private static final IsolateThreadRuleListener RULE_LISTENER = new IsolateThreadRuleListener();

    static {
        ruleProperty.addListener(RULE_LISTENER);
    }

    private IsolateThreadRuleManager() {
    }

    /**
     * 获取规则列表
     *
     * @param resource 资源名称
     * @return 隔离仓规则
     */
    public static List<IsolateThreadRule> getRules(String resource) {
        return BULK_THREAD_RULE_CACHE.get(resource);
    }

    /**
     * 注册动态配置
     *
     * @param property 动态配置
     */
    public static void register2Property(SentinelProperty<List<IsolateThreadRule>> property) {
        if (property == null) {
            return;
        }
        synchronized (RULE_LISTENER) {
            ruleProperty.removeListener(RULE_LISTENER);
            ruleProperty = property;
            ruleProperty.addListener(RULE_LISTENER);
        }
    }

    /**
     * 手动加载规则
     *
     * @param rules 隔离仓规则列表
     */
    public static void loadRules(List<IsolateThreadRule> rules) {
        ruleProperty.updateValue(rules);
    }

    static class IsolateThreadRuleListener implements PropertyListener<List<IsolateThreadRule>> {
        @Override
        public void configUpdate(List<IsolateThreadRule> rules) {
            updateRules(rules);
        }

        @Override
        public void configLoad(List<IsolateThreadRule> rules) {
            updateRules(rules);
        }

        /**
         * 更新规则
         *
         * @param rules 规则列表
         */
        private void updateRules(List<IsolateThreadRule> rules) {
            if (rules == null) {
                BULK_THREAD_RULE_CACHE.clear();
                return;
            }
            final HashMap<String, List<IsolateThreadRule>> newRules = new HashMap<String, List<IsolateThreadRule>>(
                rules.size());
            for (IsolateThreadRule rule : rules) {
                List<IsolateThreadRule> isolateThreadRules = newRules.get(rule.getResource());
                if (isolateThreadRules == null) {
                    isolateThreadRules = new ArrayList<IsolateThreadRule>();
                }
                isolateThreadRules.add(rule);
                newRules.put(rule.getResource(), isolateThreadRules);
            }
            BULK_THREAD_RULE_CACHE.clear();
            BULK_THREAD_RULE_CACHE.putAll(newRules);
        }
    }
}
