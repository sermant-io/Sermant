/*
 * Copyright (C) 2023-2024 Sermant Authors. All rights reserved.
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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

/**
 * The routing tool class of the traffic matching method
 *
 * @author lilai
 * @since 2023-02-21
 */
public class FlowRuleUtils {
    private FlowRuleUtils() {
    }

    /**
     * Get the target rule
     *
     * @param configuration Route configuration
     * @param targetService Target service
     * @param path Dubbo interface name/URL path
     * @param serviceName The name of the service
     * @return Target rules
     */
    public static List<Rule> getFlowRules(RouterConfiguration configuration, String targetService, String path,
            String serviceName) {
        if (RouterConfiguration.isInValid(configuration, RouterConstant.FLOW_MATCH_KIND)) {
            return Collections.emptyList();
        }

        List<Rule> rules = RuleUtils.getRules(configuration, targetService, RouterConstant.FLOW_MATCH_KIND);

        if (CollectionUtils.isEmpty(rules)) {
            return Collections.emptyList();
        }

        List<Rule> list = new ArrayList<>();
        for (Rule rule : rules) {
            if (isTargetFlowRule(rule, path, serviceName)) {
                list.add(rule);
            }
        }
        return list;
    }

    /**
     * Get the target rule
     *
     * @param rule Routing rules
     * @param path Dubbo interface name/URL path
     * @param serviceName The name of the service
     * @return Whether it is a target rule
     */
    private static boolean isTargetFlowRule(Rule rule, String path, String serviceName) {
        if (rule == null) {
            return false;
        }
        Match match = rule.getMatch();
        if (match != null) {
            String source = match.getSource();
            if (StringUtils.isExist(source) && !source.equals(serviceName)) {
                return false;
            }
            String matchPath = match.getPath();
            if (!CollectionUtils.isEmpty(match.getAttachments()) || !CollectionUtils.isEmpty(match.getHeaders())) {
                if (StringUtils.isExist(matchPath) && !Pattern.matches(matchPath, getInterfaceName(path))) {
                    return false;
                }
            } else if (!CollectionUtils.isEmpty(match.getArgs())) {
                if (StringUtils.isBlank(matchPath) || !matchPath.equals(path)) {
                    return false;
                }
            }
        }
        return !CollectionUtils.isEmpty(rule.getRoute());
    }

    /**
     * In the attachment and header rule matching yes, remove the method name in the interface
     *
     * @param path path
     * @return Remove the path of the method name
     */
    private static String getInterfaceName(String path) {
        String[] pathList = path.split(":");
        pathList[0] = delMethodName(pathList[0]);
        return String.join(":", pathList);
    }

    /**
     * Delete the method name
     *
     * @param path path
     * @return Delete the path of the method name
     */
    private static String delMethodName(String path) {
        ArrayList<String> list = new ArrayList<>(Arrays.asList(path.split("\\.")));
        list.remove(list.size() - 1);
        return String.join(".", list);
    }
}
