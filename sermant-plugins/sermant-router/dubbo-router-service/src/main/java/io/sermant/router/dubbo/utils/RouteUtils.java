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

package io.sermant.router.dubbo.utils;

import io.sermant.router.common.constants.RouterConstant;
import io.sermant.router.common.utils.CollectionUtils;
import io.sermant.router.config.entity.Match;
import io.sermant.router.config.entity.MatchRule;
import io.sermant.router.config.entity.MatchStrategy;
import io.sermant.router.config.entity.Route;
import io.sermant.router.config.entity.Rule;
import io.sermant.router.config.entity.ValueMatch;
import io.sermant.router.dubbo.strategy.TypeStrategyChooser;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

/**
 * routing plugin utility class
 *
 * @author provenceee
 * @since 2021-06-21
 */
public class RouteUtils {
    private RouteUtils() {
    }

    /**
     * get matching swimlanes
     *
     * @param list valid rules
     * @param attachments Dubbo's attachments parameter
     * @param arguments The argument parameter of dubbo
     * @return Matching lane markers
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
                    // As long as one of them doesn't match, it's a mismatch
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
                    // As long as one of them doesn't match, it's a mismatch
                    return false;
                }
            }
        }
        return true;
    }
}
