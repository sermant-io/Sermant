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

package com.huaweicloud.sermant.router.dubbo.utils;

import com.huaweicloud.sermant.router.common.constants.RouterConstant;
import com.huaweicloud.sermant.router.common.utils.CollectionUtils;
import com.huaweicloud.sermant.router.config.entity.Match;
import com.huaweicloud.sermant.router.config.entity.MatchRule;
import com.huaweicloud.sermant.router.config.entity.MatchStrategy;
import com.huaweicloud.sermant.router.config.entity.Route;
import com.huaweicloud.sermant.router.config.entity.Rule;
import com.huaweicloud.sermant.router.config.entity.ValueMatch;
import com.huaweicloud.sermant.router.dubbo.strategy.TypeStrategyChooser;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

/**
 * 路由插件工具类
 *
 * @author provenceee
 * @since 2021-06-21
 */
public class RouteUtils {
    private RouteUtils() {
    }

    /**
     * 获取匹配的泳道
     *
     * @param list 有效的规则
     * @param attachments dubbo的attachments参数
     * @param arguments dubbo的arguments参数
     * @return 匹配的泳道标记
     */
    public static List<Route> getLaneRoutes(List<Rule> list, Map<String, Object> attachments, Object[] arguments) {
        for (Rule rule : list) {
            Match match = rule.getMatch();
            if (match == null) {
                return rule.getRoute();
            }
            if (isMatchByAttachments(match.getAttachments(), attachments) && isMatchByArgs(match.getArgs(),
                    arguments)) {
                return rule.getRoute();
            }
        }
        return Collections.emptyList();
    }

    private static boolean isMatchByAttachments(Map<String, List<MatchRule>> matchAttachments,
            Map<String, Object> attachments) {
        if (CollectionUtils.isEmpty(matchAttachments)) {
            return true;
        }
        for (Entry<String, List<MatchRule>> entry : matchAttachments.entrySet()) {
            String key = entry.getKey();
            List<MatchRule> matchRuleList = entry.getValue();
            for (MatchRule matchRule : matchRuleList) {
                ValueMatch valueMatch = matchRule.getValueMatch();
                List<String> values = valueMatch.getValues();
                MatchStrategy matchStrategy = valueMatch.getMatchStrategy();
                String arg = Optional.ofNullable(attachments.get(key)).map(String::valueOf).orElse(null);
                if (!matchStrategy.isMatch(values, arg, matchRule.isCaseInsensitive())) {
                    // 只要一个匹配不上，那就是不匹配
                    return false;
                }
            }
        }
        return true;
    }

    private static boolean isMatchByArgs(Map<String, List<MatchRule>> matchArgs, Object[] arguments) {
        if (CollectionUtils.isEmpty(matchArgs)) {
            return true;
        }
        for (Entry<String, List<MatchRule>> entry : matchArgs.entrySet()) {
            String key = entry.getKey();
            if (!key.startsWith(RouterConstant.DUBBO_SOURCE_TYPE_PREFIX)) {
                continue;
            }
            List<MatchRule> matchRuleList = entry.getValue();
            for (MatchRule matchRule : matchRuleList) {
                ValueMatch valueMatch = matchRule.getValueMatch();
                List<String> values = valueMatch.getValues();
                MatchStrategy matchStrategy = valueMatch.getMatchStrategy();
                String arg = TypeStrategyChooser.INSTANCE.getValue(matchRule.getType(), key, arguments).orElse(null);
                if (!matchStrategy.isMatch(values, arg, matchRule.isCaseInsensitive())) {
                    // 只要一个匹配不上，那就是不匹配
                    return false;
                }
            }
        }
        return true;
    }
}