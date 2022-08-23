/*
 * Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.huaweicloud.loadbalancer;

import com.huaweicloud.loadbalancer.rule.RuleManager;
import com.huaweicloud.sermant.core.service.dynamicconfig.common.DynamicConfigEvent;

/**
 * 用于发布配置
 *
 * @author zhouss
 * @since 2022-08-16
 */
public class RuleManagerHelper {
    private static final String MATCH_GROUP_KEY = "servicecomb.matchGroup.test";
    private static final String BALANCER_KEY = "servicecomb.loadbalance.test";

    /**
     * 发布规则
     *
     * @param rule 负载均衡规则
     * @param serviceName 服务名
     */
    public static void publishRule(String serviceName, String rule) {
        final DynamicConfigEvent matchGroupEvent = buildEvent(MATCH_GROUP_KEY, getMatchGroup(serviceName));
        RuleManager.INSTANCE.resolve(matchGroupEvent);
        final DynamicConfigEvent loadbalancerEvent = buildEvent(BALANCER_KEY, getLoadbalancer(rule));
        RuleManager.INSTANCE.resolve(loadbalancerEvent);
    }

    private static String getLoadbalancer(String rule) {
        return "rule: " + rule;
    }

    private static String getMatchGroup(String serviceName) {
        return "alias: flowcontrol111\n"
                + "matches:\n"
                + "- apiPath:\n"
                + "    exact: /sc/provider/\n"
                + "  headers: {}\n"
                + "  method:\n"
                + "  - GET\n"
                + "  name: degrade\n"
                + "  showAlert: false\n"
                + "  uniqIndex: c3w7x\n"
                + ((serviceName != null) ? ("  serviceName: " + serviceName + "\n") : "");
    }

    private static DynamicConfigEvent buildEvent(String key, String content) {
        return DynamicConfigEvent.createEvent(key, "default", content);
    }
}
