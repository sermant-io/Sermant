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

package com.huaweicloud.sermant.router.spring.strategy;

import com.huaweicloud.sermant.router.config.entity.Route;
import com.huaweicloud.sermant.router.config.strategy.AbstractRuleStrategy;
import com.huaweicloud.sermant.router.spring.strategy.mapper.AbstractMetadataMapper;
import com.huaweicloud.sermant.router.spring.strategy.mapper.DefaultMetadataMapper;
import com.huaweicloud.sermant.router.spring.strategy.mapper.EurekaMetadataMapper;
import com.huaweicloud.sermant.router.spring.strategy.mapper.ZookeeperMetadataMapper;
import com.huaweicloud.sermant.router.spring.strategy.rule.InstanceRuleStrategy;

import java.util.HashMap;
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
    INSTANCE();

    private final Map<String, AbstractMetadataMapper<Object>> map;

    private final DefaultMetadataMapper defaultMetadataMapper;

    private volatile AbstractRuleStrategy<Object> ruleStrategy;

    RuleStrategyHandler() {
        map = new HashMap<>();
        defaultMetadataMapper = new DefaultMetadataMapper();
        init(new EurekaMetadataMapper());
        init(new ZookeeperMetadataMapper());
    }

    private void init(AbstractMetadataMapper<Object> mapper) {
        for (String name : mapper.getName()) {
            map.put(name, mapper);
        }
    }

    /**
     * 选取路由匹配的实例
     *
     * @param serviceName 服务名
     * @param instances 实例列表
     * @param routes 路由规则
     * @return 路由匹配的实例
     */
    public List<Object> getMatchInstances(String serviceName, List<Object> instances, List<Route> routes) {
        return getRuleStrategy(instances).getMatchInstances(serviceName, instances, routes, false);
    }

    /**
     * 选取路由匹配的实例
     *
     * @param serviceName 服务名
     * @param instances 实例列表
     * @param tags 标签
     * @return 路由匹配的实例
     */
    public List<Object> getMatchInstancesByRequest(String serviceName, List<Object> instances,
        Map<String, String> tags) {
        return getRuleStrategy(instances).getMatchInstancesByRequest(serviceName, instances, tags);
    }

    /**
     * 选取不匹配标签的实例
     *
     * @param serviceName 服务名
     * @param instances 实例列表
     * @param tags 标签
     * @param isReturnAllInstancesWhenMismatch 无匹配时，是否返回全部实例
     * @return 路由过滤后的实例
     */
    public List<Object> getMismatchInstances(String serviceName, List<Object> instances,
        List<Map<String, String>> tags, boolean isReturnAllInstancesWhenMismatch) {
        return getRuleStrategy(instances)
            .getMismatchInstances(serviceName, instances, tags, isReturnAllInstancesWhenMismatch);
    }

    /**
     * 选取同区域的实例
     *
     * @param serviceName 服务名
     * @param instances 实例列表
     * @param zone 区域
     * @return 路由过滤后的实例
     */
    public List<Object> getZoneInstances(String serviceName, List<Object> instances, String zone) {
        return getRuleStrategy(instances).getZoneInstances(serviceName, instances, zone);
    }

    private AbstractRuleStrategy<Object> getRuleStrategy(List<Object> instances) {
        if (ruleStrategy == null) {
            synchronized (RuleStrategyHandler.class) {
                if (ruleStrategy == null) {
                    ruleStrategy = new InstanceRuleStrategy<>(getMetadataMapper(instances.get(0)));
                }
            }
        }
        return ruleStrategy;
    }

    private AbstractMetadataMapper<Object> getMetadataMapper(Object obj) {
        return map.getOrDefault(obj.getClass().getName(), defaultMetadataMapper);
    }
}