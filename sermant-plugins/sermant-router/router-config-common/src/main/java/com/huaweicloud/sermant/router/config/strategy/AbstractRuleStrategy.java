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

package com.huaweicloud.sermant.router.config.strategy;

import com.huaweicloud.sermant.core.common.LoggerFactory;
import com.huaweicloud.sermant.router.common.event.PolicyEvent;
import com.huaweicloud.sermant.router.common.utils.CollectionUtils;
import com.huaweicloud.sermant.router.config.entity.Match;
import com.huaweicloud.sermant.router.config.entity.Policy;
import com.huaweicloud.sermant.router.config.entity.Route;
import com.huaweicloud.sermant.router.config.entity.Rule;
import com.huaweicloud.sermant.router.config.utils.PolicyEventUtils;
import com.huaweicloud.sermant.router.config.utils.RuleUtils;
import com.huaweicloud.sermant.router.config.utils.RuleUtils.RouteResult;

import com.alibaba.fastjson.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 匹配策略
 *
 * @param <I> 实例泛型
 * @author provenceee
 * @since 2021-10-14
 */
public abstract class AbstractRuleStrategy<I> implements RuleStrategy<I> {
    private static final Logger LOGGER = LoggerFactory.getLogger();

    private final InstanceStrategy<I, Map<String, String>> matchInstanceStrategy;

    private final InstanceStrategy<I, List<Map<String, String>>> mismatchInstanceStrategy;

    private final Function<I, Map<String, String>> mapper;

    private final String source;

    /**
     * 构造方法
     *
     * @param source 来源
     * @param matchInstanceStrategy 匹配上的策略
     * @param mismatchInstanceStrategy 匹配不上的策略
     * @param mapper 获取metadata的方法
     */
    public AbstractRuleStrategy(String source, InstanceStrategy<I, Map<String, String>> matchInstanceStrategy,
            InstanceStrategy<I, List<Map<String, String>>> mismatchInstanceStrategy, Function<I,
            Map<String, String>> mapper) {
        this.source = source;
        this.matchInstanceStrategy = matchInstanceStrategy;
        this.mismatchInstanceStrategy = mismatchInstanceStrategy;
        this.mapper = mapper;
    }

    @Override
    public List<I> getMatchInstances(String serviceName, List<I> instances, List<Route> routes) {
        RouteResult<?> result = RuleUtils.getTargetTags(routes);
        return getInstances(getStrategy(result.isMatch()), result.getTags(), serviceName, instances, true);
    }

    /**
     * 根据rule规则获取匹配上的实例
     *
     * @param serviceName 服务名
     * @param instances 实例列表
     * @param rule rule规则
     * @return 返回rule规则筛选过滤实例
     */
    @Override
    public List<I> getMatchInstances(String serviceName, List<I> instances, Rule rule) {
        RouteResult<?> result = RuleUtils.getTargetTags(rule.getRoute());
        List<I> matchInstances = getInstances(getStrategy(result.isMatch()), result.getTags(), serviceName, instances,
                false);

        // 如果匹配到的实例数为0，则返回所以实例
        if (CollectionUtils.isEmpty(matchInstances)) {
            LOGGER.fine("not matched, return all instances");
            return instances;
        }

        // 校验是否有同TAG优先规则
        Match match = rule.getMatch();
        if (match == null) {
            return matchInstances;
        }
        Policy policy = match.getPolicy();
        if (policy == null) {
            LOGGER.fine("The same Tag priority rule is not configured (the Policy configuration is null)");
            return matchInstances;
        }

        // 情况一：全部实例最小可用阈值大于全部可用实例数，则同TAG优先
        if (policy.getMinAllInstances() > instances.size()) {
            PolicyEventUtils.notifySameTagMatchedEvent(PolicyEvent.SAME_TAG_MATCH_LESS_MIN_ALL_INSTANCES,
                    match.getTags(), serviceName);
            LOGGER.fine("Same tag rule match that less than the minimum available threshold for all instances");
            return matchInstances;
        }

        // 情况二：未设置全部实例最小可用阈值，超过(大于等于)同TAG比例阈值，则同TAG优先
        // 情况三：设置了全部实例最小可用阈值，但其小于全部TAG可用实例，超过（大于等于）同TAG比例阈值，则同TAG优先
        if (matchInstances.size() >= instances.size() * policy.getTriggerThreshold()) {
            PolicyEventUtils.notifySameTagMatchedEvent(PolicyEvent.SAME_TAG_MATCH_EXCEEDED_TRIGGER_THRESHOLD,
                    match.getTags(), serviceName);
            LOGGER.fine("Same tag rule match that exceeded trigger threshold");
            return matchInstances;
        }

        // 情况四：未匹配上
        PolicyEventUtils.notifySameTagMisMatchedEvent(PolicyEvent.SAME_TAG_MISMATCH,
                match.getTags(), serviceName);
        LOGGER.fine("not matched, return all instances");
        return instances;
    }

    @Override
    public List<I> getMatchInstancesByRequest(String serviceName, List<I> instances, Map<String, String> tags) {
        if (CollectionUtils.isEmpty(tags)) {
            return Collections.emptyList();
        }
        return getInstances(matchInstanceStrategy, tags, serviceName, instances, false);
    }

    @Override
    public List<I> getMismatchInstances(String serviceName, List<I> instances, List<Map<String, String>> tags,
            boolean isReturnAllInstancesWhenMismatch) {
        return getInstances(mismatchInstanceStrategy, tags, serviceName, instances, isReturnAllInstancesWhenMismatch);
    }

    private <T> List<I> getInstances(InstanceStrategy<I, T> instanceStrategy, T tags, String serviceName,
            List<I> instances, boolean isReturnAllInstancesWhenMismatch) {
        List<I> resultList = new ArrayList<>();
        for (I instance : instances) {
            if (instanceStrategy.isMatch(instance, tags, mapper)) {
                resultList.add(instance);
            }
        }
        boolean mismatch = CollectionUtils.isEmpty(resultList);
        if (!mismatch) {
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.fine(String.format(Locale.ROOT, "Match instances, %s serviceName is %s, tags is %s.", source,
                        serviceName, JSONObject.toJSONString(tags)));
            }
        } else if (isReturnAllInstancesWhenMismatch) {
            LOGGER.warning(String.format(Locale.ROOT,
                    "Cannot match instances, will return all instances, %s serviceName is %s, tags is %s.", source,
                    serviceName, JSONObject.toJSONString(tags)));
        } else {
            LOGGER.warning(String.format(Locale.ROOT,
                    "Cannot match instances, will return empty instances, %s serviceName is %s, tags is %s.", source,
                    serviceName, JSONObject.toJSONString(tags)));
        }
        return isReturnAllInstancesWhenMismatch && mismatch ? instances : resultList;
    }

    private <T> InstanceStrategy<I, T> getStrategy(boolean isMatch) {
        return isMatch ? (InstanceStrategy<I, T>) matchInstanceStrategy
                : (InstanceStrategy<I, T>) mismatchInstanceStrategy;
    }
}