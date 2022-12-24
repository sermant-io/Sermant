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

import com.huaweicloud.sermant.router.config.entity.Route;
import com.huaweicloud.sermant.router.config.strategy.RuleStrategy;
import com.huaweicloud.sermant.router.dubbo.strategy.rule.InvokerRuleStrategy;

import java.util.List;
import java.util.Map;

/**
 * 路由策略处理器
 *
 * @author provenceee
 * @since 2021-10-14
 */
public enum RuleStrategyHandler {
    /**
     * 实例
     */
    INSTANCE;

    private final RuleStrategy<Object> ruleStrategy;

    RuleStrategyHandler() {
        this.ruleStrategy = new InvokerRuleStrategy();
    }

    /**
     * 选取标签应用的invokers
     *
     * @param serviceName 服务名
     * @param invokers dubbo invokers
     * @param routes 路由规则
     * @return 标签应用的invokers
     */
    public List<Object> getMatchInvokers(String serviceName, List<Object> invokers, List<Route> routes) {
        return ruleStrategy.getMatchInstances(serviceName, invokers, routes, true);
    }

    /**
     * 选取路由匹配的实例
     *
     * @param serviceName 服务名
     * @param instances 实例列表
     * @param tags 标签
     * @return 路由匹配的实例
     */
    public List<Object> getMatchInvokersByRequest(String serviceName, List<Object> instances,
        Map<String, String> tags) {
        return ruleStrategy.getMatchInstancesByRequest(serviceName, instances, tags);
    }

    /**
     * 选取不匹配标签的实例
     *
     * @param serviceName 服务名
     * @param invokers 实例列表
     * @param tags 标签
     * @param isReturnAllInstancesWhenMismatch 无匹配时，是否返回全部实例
     * @return 路由过滤后的实例
     */
    public List<Object> getMismatchInvokers(String serviceName, List<Object> invokers, List<Map<String, String>> tags,
        boolean isReturnAllInstancesWhenMismatch) {
        return ruleStrategy.getMismatchInstances(serviceName, invokers, tags, isReturnAllInstancesWhenMismatch);
    }

    /**
     * 选取同区域的实例
     *
     * @param serviceName 服务名
     * @param invokers 实例列表
     * @param zone 区域
     * @return 路由过滤后的实例
     */
    public List<Object> getZoneInvokers(String serviceName, List<Object> invokers, String zone) {
        return ruleStrategy.getZoneInstances(serviceName, invokers, zone);
    }
}