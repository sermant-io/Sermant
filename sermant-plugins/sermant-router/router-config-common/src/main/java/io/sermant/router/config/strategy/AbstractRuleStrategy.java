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

package io.sermant.router.config.strategy;

import com.alibaba.fastjson.JSONObject;

import io.sermant.core.common.LoggerFactory;
import io.sermant.router.common.event.PolicyEvent;
import io.sermant.router.common.utils.CollectionUtils;
import io.sermant.router.config.entity.Match;
import io.sermant.router.config.entity.Policy;
import io.sermant.router.config.entity.Rule;
import io.sermant.router.config.utils.PolicyEventUtils;
import io.sermant.router.config.utils.RuleUtils;
import io.sermant.router.config.utils.RuleUtils.RouteResult;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Matching Policies
 *
 * @param <I> Instance generics
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
     * Constructor
     *
     * @param source Source
     * @param matchInstanceStrategy Match the policy on it
     * @param mismatchInstanceStrategy Strategies that don't match
     * @param mapper Methods to obtain metadata
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
    public List<I> getFlowMatchInstances(String serviceName, List<I> instances, Rule rule) {
        RouteResult<?> result = RuleUtils.getTargetTags(rule.getRoute());
        if (CollectionUtils.isEmpty(rule.getFallback())) {
            return getInstances(getStrategy(result.isMatch()), result.getTags(), serviceName, instances, true);
        }
        if (result.isMatch()) {
            // If a route tag is hit and a route rule is set for fallback, only matching instances are returned
            List<I> routeInstances = getInstances(getStrategy(result.isMatch()), result.getTags(), serviceName,
                    instances, false);
            if (!CollectionUtils.isEmpty(routeInstances)) {
                return routeInstances;
            }
        }

        // If the destination label set by route does not match the instance, a route is set on the fallback to route
        // the destination label instance through the fallback
        RouteResult<?> fallback = RuleUtils.getTargetTags(rule.getFallback());
        if (fallback.isMatch()) {
            List<I> fallbackInstances = getInstances(getStrategy(fallback.isMatch()), fallback.getTags(), serviceName,
                    instances, false);

            // If a route tag is set in the fallback and the corresponding instances match, the instance is selected
            // based on the fallback routing rule and the corresponding instance is returned
            if (!CollectionUtils.isEmpty(fallbackInstances)) {
                return fallbackInstances;
            }
        }

        // Combined with the above logic result.isMatch() is true, if the instance fails to be matched if true,
        // all instances will be returned directly, otherwise mismatch will be processed
        return result.isMatch() ? instances
                : getInstances(getStrategy(result.isMatch()), result.getTags(), serviceName, instances, true);
    }

    /**
     * Obtain the matching instance based on the rule
     *
     * @param serviceName Service name
     * @param instances List of instances
     * @param rule rule rules
     * @return Returns rule rules to filter and filter instances
     */
    @Override
    public List<I> getMatchInstances(String serviceName, List<I> instances, Rule rule) {
        RouteResult<?> result = RuleUtils.getTargetTags(rule.getRoute());
        List<I> matchInstances = getInstances(getStrategy(result.isMatch()), result.getTags(), serviceName, instances,
                false);

        // If the number of matched instances is 0, all instances are returned
        if (CollectionUtils.isEmpty(matchInstances)) {
            LOGGER.fine("not matched, return all instances");
            return instances;
        }

        // Check whether there is a priority rule with TAG
        Match match = rule.getMatch();
        if (match == null) {
            return matchInstances;
        }
        Policy policy = match.getPolicy();
        if (policy == null) {
            LOGGER.fine("The same Tag priority rule is not configured (the Policy configuration is null)");
            return matchInstances;
        }

        // Scenario 1: If the minimum available threshold of all instances is greater than the number of all
        // available instances, the same TAG takes precedence
        if (policy.getMinAllInstances() > instances.size()) {
            PolicyEventUtils.notifySameTagMatchedEvent(PolicyEvent.SAME_TAG_MATCH_LESS_MIN_ALL_INSTANCES,
                    match.getTags(), serviceName);
            LOGGER.fine("Same tag rule match that less than the minimum available threshold for all instances");
            return matchInstances;
        }

        // Scenario 2: If the minimum available threshold for all instances is not set and exceeds
        // (greater than or equal to) the threshold of the same TAG, the same TAG takes precedence
        // Scenario 3: If the minimum available threshold for all instances is set, but it is less than all available
        // TAG instances, and the threshold is greater than or greater than the ratio of the same TAG,
        // the same TAG takes precedence
        if (matchInstances.size() >= instances.size() * policy.getTriggerThreshold()) {
            PolicyEventUtils.notifySameTagMatchedEvent(PolicyEvent.SAME_TAG_MATCH_EXCEEDED_TRIGGER_THRESHOLD,
                    match.getTags(), serviceName);
            LOGGER.fine("Same tag rule match that exceeded trigger threshold");
            return matchInstances;
        }

        // Scenario 4: Unmatched
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
