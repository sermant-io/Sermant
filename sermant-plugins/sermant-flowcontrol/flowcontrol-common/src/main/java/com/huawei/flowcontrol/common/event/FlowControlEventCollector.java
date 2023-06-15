/*
 * Copyright (C) 2023-2023 Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.huawei.flowcontrol.common.event;

import com.huawei.flowcontrol.common.core.match.MatchManager;
import com.huawei.flowcontrol.common.core.match.RequestMatcher;

import com.huaweicloud.sermant.core.config.ConfigManager;
import com.huaweicloud.sermant.core.event.Event;
import com.huaweicloud.sermant.core.event.EventCollector;
import com.huaweicloud.sermant.core.event.EventInfo;
import com.huaweicloud.sermant.core.event.EventLevel;
import com.huaweicloud.sermant.core.event.EventManager;
import com.huaweicloud.sermant.core.event.EventType;
import com.huaweicloud.sermant.core.event.config.EventConfig;

import com.alibaba.fastjson.JSON;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 流控插件事件采集器
 *
 * @author xuezechao
 * @since 2023-04-17
 */
public class FlowControlEventCollector extends EventCollector {
    private static volatile FlowControlEventCollector flowControlEventCollector;

    private final EventConfig eventConfig = ConfigManager.getConfig(EventConfig.class);

    private FlowControlEventCollector() {
    }

    /**
     * 获取流控插件事件采集器单例
     *
     * @return 流控插件事件采集器单例
     */
    public static FlowControlEventCollector getInstance() {
        if (flowControlEventCollector == null) {
            synchronized (FlowControlEventCollector.class) {
                if (flowControlEventCollector == null) {
                    flowControlEventCollector = new FlowControlEventCollector();
                    EventManager.registerCollector(flowControlEventCollector);
                }
            }
        }
        return flowControlEventCollector;
    }

    /**
     * 采集流控规则生效事件
     *
     * @param scope 范围
     * @param name 事件名称
     * @param info 事件信息
     */
    public void collectFlowControlRuleEvent(String scope, String name, String info) {
        if (!eventConfig.isEnable()) {
            return;
        }
        offerEvent(new Event(scope, EventLevel.NORMAL, EventType.OPERATION,
                new EventInfo(name, info)));
    }

    /**
     * 采集流控配置生效事件
     *
     * @param event 事件
     */
    public void collectRuleEffectEvent(FlowControlEventEntity event) {
        if (!eventConfig.isEnable()) {
            return;
        }
        offerEvent(new Event(event.getScope(), event.getEventLevel(), event.getEventType(),
                new EventInfo(event.getName(), event.getDescription())));
    }

    /**
     * 采集触发业务流控的事件
     *
     * @param businessNames 业务场景名
     */
    public void collectBusinessEvent(Set<String> businessNames) {
        if (!eventConfig.isEnable() || businessNames == null || businessNames.isEmpty()) {
            return;
        }
        Object[] businessNamesArray = businessNames.toArray();
        Map<String, List<RequestMatcher>> businessMatcherRule = new HashMap<>();
        for (Object o : businessNamesArray) {
            businessMatcherRule.put(o.toString(),
                    MatchManager.INSTANCE.getMatchGroups(o.toString()).get(o.toString()).getMatches());
        }
        FlowControlEventEntity event = FlowControlEventEntity.FLOW_MATCH_SUCCESS;
        offerEvent(new Event(event.getScope(), event.getEventLevel(), event.getEventType(),
                new EventInfo(event.getName(),
                        event.getDescription() + ", rule:" + JSON.toJSONString(businessMatcherRule))));
    }
}
