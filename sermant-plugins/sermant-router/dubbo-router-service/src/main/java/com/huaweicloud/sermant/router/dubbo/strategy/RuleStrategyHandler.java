/*
 * Copyright (C) 2021-2022 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huaweicloud.sermant.router.dubbo.strategy;

import com.huaweicloud.sermant.router.common.mapper.AbstractMetadataMapper;
import com.huaweicloud.sermant.router.common.mapper.DefaultMapper;
import com.huaweicloud.sermant.router.config.entity.Rule;
import com.huaweicloud.sermant.router.config.strategy.RuleStrategy;
import com.huaweicloud.sermant.router.dubbo.strategy.rule.InvokerRuleStrategy;

import java.util.List;
import java.util.Map;

/**
 * routing policy handler
 *
 * @author provenceee
 * @since 2021-10-14
 */
public enum RuleStrategyHandler {
    /**
     * instance
     */
    INSTANCE;

    private RuleStrategy<Object> ruleStrategy = new InvokerRuleStrategy(new DefaultMapper());

    /**
     * Building metadata processing classes for Dubbo3.x application registration
     *
     * @param mapper Meta processing of metadata mapper
     */
    public void builedDubbo3Mapper(AbstractMetadataMapper<Object> mapper) {
        ruleStrategy = new InvokerRuleStrategy(mapper);
    }

    /**
     * Select invoker for tag application
     *
     * @param serviceName service name
     * @param invokers dubbo invokers
     * @param rule routing rules
     * @return invokers for tag applications
     */
    public List<Object> getFlowMatchInvokers(String serviceName, List<Object> invokers, Rule rule) {
        return ruleStrategy.getFlowMatchInstances(serviceName, invokers, rule);
    }

    /**
     * Select the invoker for label application based on the rule
     *
     * @param serviceName service name
     * @param invokers dubbo invokers
     * @param rule rule
     * @return invokers for tag applications
     */
    public List<Object> getMatchInvokers(String serviceName, List<Object> invokers, Rule rule) {
        return ruleStrategy.getMatchInstances(serviceName, invokers, rule);
    }

    /**
     * select the instance of route matching
     *
     * @param serviceName service name
     * @param instances list of instances
     * @param tags Label
     * @return instances of route matching
     */
    public List<Object> getMatchInvokersByRequest(String serviceName, List<Object> instances,
            Map<String, String> tags) {
        return ruleStrategy.getMatchInstancesByRequest(serviceName, instances, tags);
    }

    /**
     * select instances of mismatched labels
     *
     * @param serviceName service name
     * @param invokers list of instances
     * @param tags Label
     * @param isReturnAllInstancesWhenMismatch If there is no match, whether to return all instances
     * @return instances that are route filtered
     */
    public List<Object> getMismatchInvokers(String serviceName, List<Object> invokers, List<Map<String, String>> tags,
            boolean isReturnAllInstancesWhenMismatch) {
        return ruleStrategy.getMismatchInstances(serviceName, invokers, tags, isReturnAllInstancesWhenMismatch);
    }
}