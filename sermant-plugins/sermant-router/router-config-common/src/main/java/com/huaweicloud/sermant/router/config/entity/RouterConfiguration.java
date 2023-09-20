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

package com.huaweicloud.sermant.router.config.entity;

import com.huaweicloud.sermant.router.common.utils.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 路由标签
 *
 * @author provenceee
 * @since 2021-10-27
 */
public class RouterConfiguration {
    /**
     * 标签规则,key为应用名，value为规则
     */
    private final Map<String, List<Rule>> routeRule = new ConcurrentHashMap<>();

    /**
     * 全局路由规则
     */
    private final List<Rule> globalRules = new CopyOnWriteArrayList<>();

    public Map<String, List<Rule>> getRouteRule() {
        return routeRule;
    }

    public List<Rule> getGlobalRules() {
        return globalRules;
    }

    /**
     * 获取指定服务的路由规则
     *
     * @param serviceName 服务名
     * @return 路由规则
     */
    public List<Rule> getRules(String serviceName) {
        if (CollectionUtils.isEmpty(routeRule) && CollectionUtils.isEmpty(globalRules)) {
            return Collections.emptyList();
        }
        return routeRule.getOrDefault(serviceName, globalRules);
    }

    /**
     * 重置路由规则
     *
     * @param map 路由规则
     */
    public void resetRouteRule(Map<String, List<Rule>> map) {
        routeRule.clear();
        routeRule.putAll(map);
    }

    /**
     * 重置全局路由规则
     *
     * @param rules 全局路由规则
     */
    public void resetGlobalRules(List<Rule> rules) {
        globalRules.clear();
        globalRules.addAll(rules);
    }

    /**
     * 路由规则是否无效
     *
     * @param configuration 路由规则
     * @return 是否无效
     */
    public static boolean isInValid(RouterConfiguration configuration) {
        return configuration == null || (CollectionUtils.isEmpty(configuration.getRouteRule())
            && CollectionUtils.isEmpty(configuration.getGlobalRules()));
    }

    /**
     * 路由规则是否无效
     *
     * @param configuration 路由规则
     * @param serviceName 服务名
     * @return 是否无效
     */
    public static boolean isInValid(RouterConfiguration configuration, String serviceName) {
        return configuration == null || CollectionUtils.isEmpty(configuration.getRules(serviceName));
    }
}