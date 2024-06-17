/*
 * Copyright (C) 2024-2024 Sermant Authors. All rights reserved.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package io.sermant.mq.grayscale.strategy;

import io.sermant.core.utils.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * matcher for traffic/environment tags
 *
 * @author chengyouling
 * @since 2024-06-03
 */
public class TagKeyMatcher {
    private static final Map<String, MatchStrategy> STRATEGY_MAP = new HashMap<>();

    private static final String EXACT_RULE_KEY = "exact";

    static {
        STRATEGY_MAP.put(EXACT_RULE_KEY, new ExactMatchStrategy());
    }

    private TagKeyMatcher() {
    }

    /**
     * Whether the key in the request or thread variable matches the rule to be transmitted in the configuration
     *
     * @param properties the matched properties
     * @param matchRule the matched rule
     * @return matching result
     */
    public static String getMatchTag(Map<String, List<String>> matchRule, Map<String, String> properties) {
        if (!properties.isEmpty() && !matchRule.isEmpty()) {
            for (String rule : matchRule.keySet()) {
                MatchStrategy matchStrategy = STRATEGY_MAP.get(rule);
                String matchTag = matchStrategy.getMatchTag(properties, matchRule.get(rule));
                if (!StringUtils.isEmpty(matchTag)) {
                    return matchTag;
                }
            }
        }
        return "";
    }
}
