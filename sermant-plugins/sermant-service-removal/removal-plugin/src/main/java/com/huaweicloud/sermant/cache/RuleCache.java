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

package com.huaweicloud.sermant.cache;

import com.huaweicloud.sermant.config.RemovalConfig;
import com.huaweicloud.sermant.config.RemovalRule;
import com.huaweicloud.sermant.core.config.ConfigManager;
import com.huaweicloud.sermant.core.utils.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 规则缓存类
 *
 * @author zhp
 * @since 2023-02-17
 */
public class RuleCache {
    /**
     * 规则缓存MAP
     */
    protected static final Map<String, RemovalRule> RULE_MAP = new HashMap<>();

    private static final RemovalRule RULE;

    private static final RemovalConfig REMOVAL_CONFIG = ConfigManager.getConfig(RemovalConfig.class);

    private RuleCache() {
    }

    static {
        List<RemovalRule> rules = REMOVAL_CONFIG.getRules();
        if (rules != null && !rules.isEmpty()) {
            rules.forEach(removalRule -> RULE_MAP.put(StringUtils.getString(removalRule.getKey()), removalRule));
        }
        RULE = RULE_MAP.get(StringUtils.EMPTY);
    }

    /**
     * 获取规则信息
     *
     * @param key 维度信息
     * @return 离群实例摘除规则
     */
    public static Optional<RemovalRule> getRule(String key) {
        return Optional.ofNullable(RULE_MAP.getOrDefault(key, RULE));
    }
}
