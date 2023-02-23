/*
 * Copyright (C) 2023-2023 Huawei Technologies Co., Ltd. All rights reserved.
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

import com.huaweicloud.sermant.router.common.constants.RouterConstant;
import com.huaweicloud.sermant.router.config.cache.ConfigCache;
import com.huaweicloud.sermant.router.config.entity.Protocol;
import com.huaweicloud.sermant.router.config.entity.Route;
import com.huaweicloud.sermant.router.config.entity.RouterConfiguration;
import com.huaweicloud.sermant.router.config.entity.Rule;
import com.huaweicloud.sermant.router.config.utils.RuleUtils;
import com.huaweicloud.sermant.router.spring.cache.AppCache;
import com.huaweicloud.sermant.router.spring.utils.RouteUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * LaneHandlerInterceptor的service
 *
 * @author provenceee
 * @since 2023-02-20
 */
public class LaneServiceImpl implements LaneService {
    @Override
    public Map<String, List<String>> getLaneByParameterArray(String path, String methodName,
        Map<String, List<String>> headers, Map<String, String[]> parameters) {
        List<Rule> rules = getRules(path, methodName);
        List<Route> routes = RouteUtils.getLaneRoutesByParameterArray(rules, headers, parameters);
        return RuleUtils.getTargetLaneTags(routes);
    }

    @Override
    public Map<String, List<String>> getLaneByParameterList(String path, String methodName,
        Map<String, List<String>> headers, Map<String, List<String>> parameters) {
        List<Rule> rules = getRules(path, methodName);
        List<Route> routes = RouteUtils.getLaneRoutesByParameterList(rules, headers, parameters);
        return RuleUtils.getTargetLaneTags(routes);
    }

    private List<Rule> getRules(String path, String methodName) {
        RouterConfiguration configuration = ConfigCache.getLabel(RouterConstant.SPRING_CACHE_NAME);
        if (RouterConfiguration.isInValid(configuration)) {
            return Collections.emptyList();
        }
        return RuleUtils
            .getLaneRules(configuration, methodName, path, AppCache.INSTANCE.getAppName(), Protocol.HTTP);
    }
}