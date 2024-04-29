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

package io.sermant.removal.event;

import io.sermant.core.event.EventLevel;
import io.sermant.core.event.EventType;

/**
 * Outlier event enumeration
 *
 * @author zhp
 * @since 2023-02-27
 */
public enum RemovalEventDefinitions {
    /**
     * Outlier instance removal event
     */
    INSTANCE_REMOVAL("INSTANCE_REMOVAL", EventType.GOVERNANCE, EventLevel.IMPORTANT, "removal",
            "The outlier instance is removed and the instance information is: "),

    /**
     * Outlier instance recovery event
     */
    INSTANCE_RECOVERY("INSTANCE_RECOVERY", EventType.GOVERNANCE, EventLevel.IMPORTANT, "removal",
            "The outlier instance is recovery and the instance information is: "),;

    /**
     * The name of the event
     */
    private final String name;

    /**
     * The type of event
     */
    private final EventType eventType;

    /**
     * Event level
     */
    private final EventLevel eventLevel;

    /**
     * Event area
     */
    private final String scope;

    /**
     * Description of the event
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
