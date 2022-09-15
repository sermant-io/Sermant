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

package com.huaweicloud.sermant.router.config.label.entity;

import com.huaweicloud.sermant.router.common.utils.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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

    public Map<String, List<Rule>> getRouteRule() {
        return routeRule;
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
     * 路由规则是否无效
     *
     * @param configuration 路由规则
     * @return 是否无效
     */
    public static boolean isInValid(RouterConfiguration configuration) {
        return configuration == null || CollectionUtils.isEmpty(configuration.getRouteRule());
    }
}