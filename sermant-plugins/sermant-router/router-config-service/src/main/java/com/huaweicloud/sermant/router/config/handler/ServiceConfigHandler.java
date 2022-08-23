/*
 * Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huaweicloud.sermant.router.config.handler;

import com.huaweicloud.sermant.core.plugin.subscribe.processor.OrderConfigEvent;
import com.huaweicloud.sermant.core.service.dynamicconfig.common.DynamicConfigEvent;
import com.huaweicloud.sermant.core.service.dynamicconfig.common.DynamicConfigEventType;
import com.huaweicloud.sermant.router.common.constants.RouterConstant;
import com.huaweicloud.sermant.router.common.utils.CollectionUtils;
import com.huaweicloud.sermant.router.config.label.entity.RouterConfiguration;
import com.huaweicloud.sermant.router.config.label.entity.Rule;
import com.huaweicloud.sermant.router.config.utils.RuleUtils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * 路由配置处理器（服务维度）
 *
 * @author provenceee
 * @since 2022-08-09
 */
public class ServiceConfigHandler extends AbstractConfigHandler {
    @Override
    public void handle(DynamicConfigEvent event, RouterConfiguration configuration) {
        String serviceName = event.getKey().substring(RouterConstant.ROUTER_KEY_PREFIX.length() + 1);
        if (event.getEventType() == DynamicConfigEventType.DELETE) {
            configuration.getRouteRule().remove(serviceName);
            RuleUtils.updateHeaderKeys(serviceName, Collections.emptyList());
            return;
        }
        List<Rule> list = JSONArray.parseArray(JSONObject.toJSONString(getRule(event, serviceName)), Rule.class);
        if (CollectionUtils.isEmpty(list)) {
            configuration.getRouteRule().remove(serviceName);
            return;
        }
        for (Rule rule : list) {
            // 去掉无效的规则
            RuleUtils.removeInvalidRules(rule.getMatch());

            // 无attachments规则，将headers规则更新到attachments规则
            RuleUtils.setAttachmentsByHeaders(rule.getMatch());

            // 去掉无效的路由
            RuleUtils.removeInvalidRoute(rule.getRoute());
        }
        list.sort((o1, o2) -> o2.getPrecedence() - o1.getPrecedence());
        configuration.getRouteRule().put(serviceName, list);
        RuleUtils.updateHeaderKeys(serviceName, list);
    }

    private List<Map<String, Object>> getRule(DynamicConfigEvent event, String serviceName) {
        if (event instanceof OrderConfigEvent) {
            Map<String, Object> allData = ((OrderConfigEvent) event).getAllData();
            Map<String, List<Map<String, Object>>> routeRuleMap = new HashMap<>();
            for (Entry<String, Object> entry : allData.entrySet()) {
                String key = entry.getKey();
                if (!key.startsWith(RouterConstant.ROUTER_KEY_PREFIX + ".")) {
                    continue;
                }
                Object value = entry.getValue();
                if (value instanceof String) {
                    routeRuleMap.put(entry.getKey(), yaml.loadAs((String) value, List.class));
                } else {
                    routeRuleMap.put(entry.getKey(), (List<Map<String, Object>>) value);
                }
            }
            return routeRuleMap.get(RouterConstant.ROUTER_KEY_PREFIX + "." + serviceName);
        }
        return yaml.loadAs(event.getContent(), List.class);
    }
}