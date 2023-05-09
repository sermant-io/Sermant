/*
 * Copyright (C) 2023-2023 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huawei.flowcontrol.res4j.util;

import com.huawei.flowcontrol.common.event.FlowControlEventCollector;
import com.huawei.flowcontrol.common.event.FlowControlEventEntity;

import java.util.concurrent.ConcurrentHashMap;

/**
 * 流控事件工具类
 *
 * @author xuezechao1
 * @since 2023-05-08
 */
public class FlowControlEventUtils {

    private static final ConcurrentHashMap<String, FlowControlEventEntity> FLOWCONTROL_EVENT_CONCURRENT_HASH_MAP
            = new ConcurrentHashMap<>();

    static {
        FLOWCONTROL_EVENT_CONCURRENT_HASH_MAP.put("Bulkhead",
                FlowControlEventEntity.FLOW_CONTROL_BULKHEAD_DISENABLE);
        FLOWCONTROL_EVENT_CONCURRENT_HASH_MAP.put("Circuit",
                FlowControlEventEntity.FLOW_CONTROL_CIRCUITBREAKER_DISENABLE);
        FLOWCONTROL_EVENT_CONCURRENT_HASH_MAP.put("Fault",
                FlowControlEventEntity.FLOW_CONTROL_FAULTINJECTION_DISENABLE);
        FLOWCONTROL_EVENT_CONCURRENT_HASH_MAP.put("RateLimiting",
                FlowControlEventEntity.FLOW_CONTROL_RATELIMITING_DISENABLE);
        FLOWCONTROL_EVENT_CONCURRENT_HASH_MAP.put("System",
                FlowControlEventEntity.FLOW_CONTROL_SYSTEM_DISENABLE);
        FLOWCONTROL_EVENT_CONCURRENT_HASH_MAP.put("InstanceIsolation",
                FlowControlEventEntity.FLOW_CONTROL_INSTANCEISOLATION_DISENABLE);
    }

    private FlowControlEventUtils() {

    }

    /**
     * 规则状态变更事件上报
     *
     * @param newState 新状态
     * @param ruleName 规则名称
     */
    public static void notifySameRuleMatchedEvent(FlowControlEventEntity newState, String ruleName) {
        FlowControlEventEntity previousState = FLOWCONTROL_EVENT_CONCURRENT_HASH_MAP.get(ruleName);
        if (!newState.equals(previousState)) {
            FLOWCONTROL_EVENT_CONCURRENT_HASH_MAP.put(ruleName, newState);
            FlowControlEventCollector.getInstance().collectRuleEffectEvent(newState);
        }
    }
}
