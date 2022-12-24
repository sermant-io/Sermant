/*
 * Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huaweicloud.sermant.router.spring.service;

import com.huaweicloud.sermant.core.plugin.config.PluginConfigManager;
import com.huaweicloud.sermant.core.utils.StringUtils;
import com.huaweicloud.sermant.router.common.config.RouterConfig;
import com.huaweicloud.sermant.router.common.constants.RouterConstant;
import com.huaweicloud.sermant.router.common.utils.CollectionUtils;
import com.huaweicloud.sermant.router.config.cache.ConfigCache;
import com.huaweicloud.sermant.router.config.entity.EnabledStrategy;
import com.huaweicloud.sermant.router.config.entity.Route;
import com.huaweicloud.sermant.router.config.entity.RouterConfiguration;
import com.huaweicloud.sermant.router.config.entity.Rule;
import com.huaweicloud.sermant.router.config.utils.RuleUtils;
import com.huaweicloud.sermant.router.spring.cache.AppCache;
import com.huaweicloud.sermant.router.spring.strategy.RuleStrategyHandler;
import com.huaweicloud.sermant.router.spring.utils.RouteUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * BaseLoadBalancerInterceptor服务
 *
 * @author provenceee
 * @since 2022-07-20
 */
public class LoadBalancerServiceImpl implements LoadBalancerService {
    private static final String VERSION_KEY = "version";

    private final RouterConfig routerConfig;

    // 用于过滤实例的tags集合，value为null，代表含有该标签的实例全部过滤，不判断value值
    private final Map<String, String> allMismatchTags;

    /**
     * 构造方法
     */
    public LoadBalancerServiceImpl() {
        routerConfig = PluginConfigManager.getPluginConfig(RouterConfig.class);
        allMismatchTags = new HashMap<>();
        for (String requestTag : routerConfig.getRequestTags()) {
            allMismatchTags.put(requestTag, null);
        }

        // 所有实例都含有version，所以不能存入null值
        allMismatchTags.remove(RouterConstant.DUBBO_VERSION_KEY);
    }

    @Override
    public List<Object> getTargetInstances(String targetName, List<Object> instances, String path,
        Map<String, List<String>> header) {
        if (!shouldHandle(instances)) {
            return instances;
        }
        return routerConfig.isUseRequestRouter() ? getTargetInstancesByRequest(targetName, instances, header)
            : getTargetInstancesByRules(targetName, instances, path, header);
    }

    @Override
    public List<Object> getZoneInstances(String targetName, List<Object> instances, boolean enabledZoneRouter) {
        EnabledStrategy strategy = ConfigCache.getEnabledStrategy(RouterConstant.SPRING_CACHE_NAME);
        if (shouldHandle(instances) && enabledZoneRouter && strategy.getStrategy()
            .isMatch(strategy.getValue(), targetName)) {
            return RuleStrategyHandler.INSTANCE.getZoneInstances(targetName, instances, routerConfig.getZone());
        }
        return instances;
    }

    private boolean shouldHandle(List<Object> instances) {
        // 实例数大于1才能路由
        return instances != null && instances.size() > 1;
    }

    private List<Object> getTargetInstancesByRules(String targetName, List<Object> instances, String path,
        Map<String, List<String>> header) {
        RouterConfiguration configuration = ConfigCache.getLabel(RouterConstant.SPRING_CACHE_NAME);
        if (RouterConfiguration.isInValid(configuration)) {
            return instances;
        }
        List<Rule> rules = RuleUtils.getRules(configuration, targetName, path, AppCache.INSTANCE.getAppName());
        List<Route> routes = RouteUtils.getRoutes(rules, header);
        if (!CollectionUtils.isEmpty(routes)) {
            return RuleStrategyHandler.INSTANCE.getMatchInstances(targetName, instances, routes);
        }
        return RuleStrategyHandler.INSTANCE
            .getMismatchInstances(targetName, instances, RuleUtils.getTags(rules, false), true);
    }

    private List<Object> getTargetInstancesByRequest(String targetName, List<Object> instances,
        Map<String, List<String>> header) {
        List<String> requestTags = routerConfig.getRequestTags();
        if (CollectionUtils.isEmpty(requestTags)) {
            return instances;
        }

        // 用于匹配实例的tags集合
        Map<String, String> tags = new HashMap<>();

        // 用于过滤实例的tags集合，value为null，代表含有该标签的实例全部过滤，不判断value值
        Map<String, String> mismatchTags = new HashMap<>();
        for (String key : header.keySet()) {
            if (!requestTags.contains(key)) {
                continue;
            }
            mismatchTags.put(key, null);
            List<String> values = header.get(key);
            if (!CollectionUtils.isEmpty(values) && StringUtils.isExist(values.get(0))) {
                tags.put(key, values.get(0));
            }
        }
        if (StringUtils.isExist(tags.get(VERSION_KEY))) {
            mismatchTags.put(VERSION_KEY, tags.get(VERSION_KEY));
        } else {
            // 所有实例都含有version，所以不能存入null值
            mismatchTags.remove(VERSION_KEY);
        }
        boolean isReturnAllInstancesWhenMismatch = false;
        if (CollectionUtils.isEmpty(mismatchTags)) {
            // 不传入header时，优先匹配无标签实例，没有无标签实例时，返回全部实例
            mismatchTags = allMismatchTags;
            isReturnAllInstancesWhenMismatch = true;
        }
        List<Object> result = RuleStrategyHandler.INSTANCE.getMatchInstancesByRequest(targetName, instances, tags);
        if (CollectionUtils.isEmpty(result)) {
            result = RuleStrategyHandler.INSTANCE.getMismatchInstances(targetName, instances,
                Collections.singletonList(mismatchTags), isReturnAllInstancesWhenMismatch);
        }
        return result;
    }
}