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
import com.huaweicloud.sermant.core.utils.StringUtils;
import com.huaweicloud.sermant.router.common.utils.CollectionUtils;
import com.huaweicloud.sermant.router.config.entity.Route;
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

    private final InstanceStrategy<I, String> zoneInstanceStrategy;

    private final Function<I, Map<String, String>> mapper;

    private final String source;

    /**
     * 构造方法
     *
     * @param source 来源
     * @param matchInstanceStrategy 匹配上的策略
     * @param mismatchInstanceStrategy 匹配不上的策略
     * @param zoneInstanceStrategy 区域路由策略
     * @param mapper 获取metadata的方法
     */
    public AbstractRuleStrategy(String source, InstanceStrategy<I, Map<String, String>> matchInstanceStrategy,
        InstanceStrategy<I, List<Map<String, String>>> mismatchInstanceStrategy,
        InstanceStrategy<I, String> zoneInstanceStrategy, Function<I, Map<String, String>> mapper) {
        this.source = source;
        this.matchInstanceStrategy = matchInstanceStrategy;
        this.mismatchInstanceStrategy = mismatchInstanceStrategy;
        this.zoneInstanceStrategy = zoneInstanceStrategy;
        this.mapper = mapper;
    }

    @Override
    public List<I> getMatchInstances(String serviceName, List<I> instances, List<Route> routes, boolean isReplaceDash) {
        RouteResult<?> result = RuleUtils.getTargetTags(routes, isReplaceDash);
        return getInstances(getStrategy(result.isMatch()), result.getTags(), serviceName, instances, true);
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

    /**
     * 选取同区域的实例
     *
     * @param instances 实例列表
     * @param zone 区域
     * @return 路由过滤后的实例
     */
    @Override
    public List<I> getZoneInstances(String serviceName, List<I> instances, String zone) {
        if (StringUtils.isBlank(zone)) {
            return instances;
        }
        return getInstances(zoneInstanceStrategy, zone, serviceName, instances, true);
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