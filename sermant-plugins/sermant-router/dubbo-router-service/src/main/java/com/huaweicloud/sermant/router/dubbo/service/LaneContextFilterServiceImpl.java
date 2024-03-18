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

package com.huaweicloud.sermant.router.dubbo.service;

import com.huaweicloud.sermant.router.common.cache.DubboCache;
import com.huaweicloud.sermant.router.common.constants.RouterConstant;
import com.huaweicloud.sermant.router.config.cache.ConfigCache;
import com.huaweicloud.sermant.router.config.entity.Protocol;
import com.huaweicloud.sermant.router.config.entity.Route;
import com.huaweicloud.sermant.router.config.entity.RouterConfiguration;
import com.huaweicloud.sermant.router.config.entity.Rule;
import com.huaweicloud.sermant.router.config.utils.RuleUtils;
import com.huaweicloud.sermant.router.dubbo.utils.RouteUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * ContextFilter的service
 *
 * @author provenceee
 * @since 2023-02-16
 */
public class LaneContextFilterServiceImpl implements LaneContextFilterService {
    /**
     * 获取泳道标记
     *
     * @param interfaceName 接口名
     * @param methodName 方法名
     * @param attachments attachments
     * @param args 接口参数
     * @return 泳道标记
     */
    @Override
    public Map<String, List<String>> getLane(String interfaceName, String methodName, Map<String, Object> attachments,
            Object[] args) {
        RouterConfiguration configuration = ConfigCache.getLabel(RouterConstant.DUBBO_CACHE_NAME);
        if (RouterConfiguration.isInValid(configuration, RouterConstant.LANE_MATCH_KIND)) {
            return Collections.emptyMap();
        }
        List<Rule> rules = RuleUtils.getLaneRules(configuration, methodName, interfaceName,
                DubboCache.INSTANCE.getAppName(), Protocol.DUBBO);
        List<Route> routes = RouteUtils.getLaneRoutes(rules, attachments, args);
        return RuleUtils.getTargetLaneTags(routes);
    }
}