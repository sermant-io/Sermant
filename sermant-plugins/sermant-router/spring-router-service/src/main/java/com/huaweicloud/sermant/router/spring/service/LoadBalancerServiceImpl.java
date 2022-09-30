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

import java.util.List;
import java.util.Map;

/**
 * BaseLoadBalancerInterceptor服务
 *
 * @author provenceee
 * @since 2022-07-20
 */
public class LoadBalancerServiceImpl implements LoadBalancerService {
    private final RouterConfig routerConfig;

    /**
     * 构造方法
     */
    public LoadBalancerServiceImpl() {
        routerConfig = PluginConfigManager.getPluginConfig(RouterConfig.class);
    }

    @Override
    public List<Object> getTargetInstances(String targetName, List<Object> instances, String path,
        Map<String, List<String>> header) {
        if (!shouldHandle(instances)) {
            return instances;
        }
        RouterConfiguration configuration = ConfigCache.getLabel(RouterConstant.SPRING_CACHE_NAME);
        List<Rule> rules = RuleUtils.getRules(configuration, targetName, path, AppCache.INSTANCE.getAppName());
        List<Route> routes = RouteUtils.getRoutes(rules, header);
        if (!CollectionUtils.isEmpty(routes)) {
            return RuleStrategyHandler.INSTANCE.getMatchInstances(targetName, instances, routes);
        }
        return RuleStrategyHandler.INSTANCE.getMismatchInstances(targetName, instances, RuleUtils.getTags(rules));
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
}