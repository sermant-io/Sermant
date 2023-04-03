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

package com.huaweicloud.sermant.event;

import com.huaweicloud.sermant.core.event.EventLevel;
import com.huaweicloud.sermant.core.event.EventType;

/**
 * 离群摘除事件枚举
 *
 * @author zhp
 * @since 2023-02-27
 */
public enum RemovalEventDefinitions {
    /**
     * 离群实例摘除事件
     */
    INSTANCE_REMOVAL("INSTANCE_REMOVAL", EventType.GOVERNANCE, EventLevel.IMPORTANT, "removal",
            "The outlier instance is removed and the instance information is: "),

    /**
     * 离群实例恢复事件
     */
    INSTANCE_RECOVERY("INSTANCE_RECOVERY", EventType.GOVERNANCE, EventLevel.IMPORTANT, "removal",
            "The outlier instance is recovery and the instance information is: "),;
    /**
     * 事件名称
     */
    private final String name;

    /**
     * 事件类型
     */
    private final EventType eventType;

    /**
     * 事件等级
     */
    private final EventLevel eventLevel;

    /**
     * 事件区域
     */
    private final String scope;

    /**
     * 事件描述
     */
    private final String description;

    RemovalEventDefinitions(String name, EventType eventType, EventLevel eventLevel, String scope, String description) {
        this.name = name;
        this.eventType = eventType;
        this.eventLevel = eventLevel;
        this.scope = scope;
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
        return scope;
    }

    public String getDescription() {
        return description;
    }
}
