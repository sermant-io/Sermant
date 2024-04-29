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

package io.sermant.tag.transmission.config.strategy;

import io.sermant.core.plugin.config.PluginConfigManager;
import io.sermant.tag.transmission.config.TagTransmissionConfig;

import java.util.HashMap;
import java.util.Map;

/**
 * matcher for transparent transmission of traffic tags
 *
 * @author lilai
 * @since 2023-09-07
 */
public class TagKeyMatcher {
    private static final Map<String, MatchStrategy> STRATEGY_MAP = new HashMap<>();

    private static final TagTransmissionConfig CONFIG = PluginConfigManager.getPluginConfig(
            TagTransmissionConfig.class);

    private static final String EXACT_RULE_KEY = "exact";

    private static final String PREFIX_RULE_KEY = "prefix";

    private static final String SUFFIX_RULE_KEY = "suffix";

    static {
        STRATEGY_MAP.put(EXACT_RULE_KEY, new ExactMatchStrategy());
        STRATEGY_MAP.put(PREFIX_RULE_KEY, new PrefixMatchStrategy());
        STRATEGY_MAP.put(SUFFIX_RULE_KEY, new SuffixMatchStrategy());
    }

    private TagKeyMatcher() {
    }

    /**
     * Whether the key in the request or thread variable matches the rule to be transmitted in the configuration
     *
     * @param key the matched key
     * @return matching result
     */
    public static boolean isMatch(String key) {
        for (String rule : CONFIG.getMatchRule().keySet()) {
            MatchStrategy matchStrategy = STRATEGY_MAP.get(rule);
            if (matchStrategy.isMatch(key, CONFIG.getMatchRule().get(rule))) {
                return true;
            }
        }
        return false;
    }
}
