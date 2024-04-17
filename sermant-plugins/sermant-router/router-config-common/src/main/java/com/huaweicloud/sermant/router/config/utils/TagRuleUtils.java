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

package com.huaweicloud.sermant.router.config.utils;

import com.huaweicloud.sermant.core.utils.StringUtils;
import com.huaweicloud.sermant.router.common.constants.RouterConstant;
import com.huaweicloud.sermant.router.common.utils.CollectionUtils;
import com.huaweicloud.sermant.router.config.entity.Match;
import com.huaweicloud.sermant.router.config.entity.RouterConfiguration;
import com.huaweicloud.sermant.router.config.entity.Rule;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Routing tools for the tag matching method
 *
 * @author lilai
 * @since 2023-02-21
 */
public class TagRuleUtils {
    private TagRuleUtils() {
    }

    /**
     * Get the target rule
     *
     * @param configuration Route configuration
     * @param targetService Target service
     * @param serviceName The name of the service
     * @return Target rules
     */
    public static List<Rule> getTagRules(RouterConfiguration configuration, String targetService,
            String serviceName) {
        if (RouterConfiguration.isInValid(configuration, RouterConstant.TAG_MATCH_KIND)) {
            return Collections.emptyList();
        }

        List<Rule> rules = RuleUtils.getRules(configuration, targetService, RouterConstant.TAG_MATCH_KIND);

        if (CollectionUtils.isEmpty(rules)) {
            return Collections.emptyList();
        }

        List<Rule> list = new ArrayList<>();
        for (Rule rule : rules) {
            if (isTargetTagRule(rule, serviceName)) {
                list.add(rule);
            }
        }
        return list;
    }

    /**
     * Get the target rule
     *
     * @param rule Routing rules
     * @param serviceName The name of the service
     * @return Whether it is a target rule
     */
    private static boolean isTargetTagRule(Rule rule, String serviceName) {
        if (rule == null) {
            return false;
        }
        Match match = rule.getMatch();
        if (match != null) {
            String source = match.getSource();
            if (StringUtils.isExist(source) && !source.equals(serviceName)) {
                return false;
            }
        }
        return !CollectionUtils.isEmpty(rule.getRoute());
    }
}
