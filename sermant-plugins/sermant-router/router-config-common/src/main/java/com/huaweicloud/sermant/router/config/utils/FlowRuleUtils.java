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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

/**
 * 流量匹配方式的路由工具类
 *
 * @author lilai
 * @since 2023-02-21
 */
public class FlowRuleUtils {
    private FlowRuleUtils() {
    }

    /**
     * 获取目标规则
     *
     * @param configuration 路由配置
     * @param targetService 目标服务
     * @param path dubbo接口名/url路径
     * @param serviceName 本服务服务名
     * @return 目标规则
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
     * 获取目标规则
     *
     * @param rule 路由规则
     * @param path dubbo接口名/url路径
     * @param serviceName 本服务服务名
     * @return 是否是目标规则
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
     * 在attachment和header规则匹配是，删除接口中的方法名
     *
     * @param path path
     * @return 去除方法名的path
     */
    private static String getInterfaceName(String path) {
        String[] pathList = path.split(":");
        pathList[0] = delMethodName(pathList[0]);
        return String.join(":", pathList);
    }

    /**
     * 删除方法名
     *
     * @param path path
     * @return 删除方法名的path
     */
    private static String delMethodName(String path) {
        ArrayList<String> list = new ArrayList<>(Arrays.asList(path.split("\\.")));
        list.remove(list.size() - 1);
        return String.join(".", list);
    }
}
