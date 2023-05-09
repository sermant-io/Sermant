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

import com.huawei.flowcontrol.common.config.CommonConst;

import com.huaweicloud.sermant.core.event.EventLevel;
import com.huaweicloud.sermant.core.event.EventType;

/**
 * 流控插件事件定义
 *
 * @author xuezechao
 * @since 2023-04-17
 */
public enum FlowControlEventEntity {

    /**
     * 隔离仓生效事件
     */
    FLOW_CONTROL_BULKHEAD_ENABLE("FLOW_CONTROL_BULKHEAD_ENABLE", EventType.GOVERNANCE,
            EventLevel.NORMAL, "FLOW_CONTROL_BULKHEAD_ENABLE"),

    /**
     * 熔断生效事件
     */
    FLOW_CONTROL_CIRCUITBREAKER_ENABLE("FLOW_CONTROL_CIRCUITBREAKER_ENABLE", EventType.GOVERNANCE,
            EventLevel.NORMAL, "FLOW_CONTROL_CIRCUITBREAKER_ENABLE"),

    /**
     * 错误注入生效事件
     */
    FLOW_CONTROL_FAULTINJECTION_ENABLE("FLOW_CONTROL_FAULTINJECTION_ENABLE", EventType.GOVERNANCE,
            EventLevel.NORMAL, "FLOW_CONTROL_FAULTINJECTION_ENABLE"),

    /**
     * 实例隔离生效事件
     */
    FLOW_CONTROL_INSTANCEISOLATION_ENABLE("FLOW_CONTROL_INSTANCEISOLATION_ENABLE", EventType.GOVERNANCE,
            EventLevel.NORMAL, "FLOW_CONTROL_INSTANCEISOLATION_ENABLE"),

    /**
     * 限流生效事件
     */
    FLOW_CONTROL_RATELIMITING_ENABLE("FLOW_CONTROL_RATELIMITING_ENABLE", EventType.GOVERNANCE,
            EventLevel.NORMAL, "FLOW_CONTROL_RATELIMITING_ENABLE"),

    /**
     * 系统流控生效事件
     */
    FLOW_CONTROL_SYSTEM_ENABLE("FLOW_CONTROL_SYSTEM_ENABLE", EventType.GOVERNANCE,
            EventLevel.NORMAL, "FLOW_CONTROL_SYSTEM_ENABLE"),

    /**
     * 隔离仓失效事件
     */
    FLOW_CONTROL_BULKHEAD_DISENABLE("FLOW_CONTROL_BULKHEAD_DISENABLE", EventType.GOVERNANCE,
            EventLevel.NORMAL, "FLOW_CONTROL_BULKHEAD_DISENABLE"),

    /**
     * 熔断失效事件
     */
    FLOW_CONTROL_CIRCUITBREAKER_DISENABLE("FLOW_CONTROL_CIRCUITBREAKER_DISENABLE", EventType.GOVERNANCE,
            EventLevel.NORMAL, "FLOW_CONTROL_CIRCUITBREAKER_DISENABLE"),

    /**
     * 错误注入失效事件
     */
    FLOW_CONTROL_FAULTINJECTION_DISENABLE("FLOW_CONTROL_FAULTINJECTION_DISENABLE", EventType.GOVERNANCE,
            EventLevel.NORMAL, "FLOW_CONTROL_FAULTINJECTION_DISENABLE"),

    /**
     * 实例隔离失效事件
     */
    FLOW_CONTROL_INSTANCEISOLATION_DISENABLE("FLOW_CONTROL_INSTANCEISOLATION_DISENABLE", EventType.GOVERNANCE,
            EventLevel.NORMAL, "FLOW_CONTROL_INSTANCEISOLATION_DISENABLE"),

    /**
     * 限流失效事件
     */
    FLOW_CONTROL_RATELIMITING_DISENABLE("FLOW_CONTROL_RATELIMITING_DISENABLE", EventType.GOVERNANCE,
            EventLevel.NORMAL, "FLOW_CONTROL_RATELIMITING_DISENABLE"),

    /**
     * 系统流控失效事件
     */
    FLOW_CONTROL_SYSTEM_DISENABLE("FLOW_CONTROL_SYSTEM_DISENABLE", EventType.GOVERNANCE,
            EventLevel.NORMAL, "FLOW_CONTROL_SYSTEM_DISENABLE");

    private final String name;

    private final EventType eventType;

    private final EventLevel eventLevel;

    private final String description;

    FlowControlEventEntity(String name, EventType eventType, EventLevel eventLevel, String description) {
        this.name = name;
        this.eventType = eventType;
        this.eventLevel = eventLevel;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public EventType getEventType() {
        return eventType;
    }

    public EventLevel getEventLevel() {
        return eventLevel;
    }

    public String getScope() {
        return CommonConst.PLUGIN_NAME;
    }

    public String getDescription() {
        return description;
    }
}
