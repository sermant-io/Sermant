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

package com.huawei.discovery.event;

import com.huaweicloud.sermant.core.event.EventLevel;
import com.huaweicloud.sermant.core.event.EventType;

/**
 * Springboot registration plugin event definition
 *
 * @author lilai
 * @since 2023-04-14
 */
public enum SpringBootRegistryEventDefinition {
    /**
     * SpringBoot service registration event
     */
    SPRINGBOOT_REGISTRY("SPRINGBOOT_REGISTRY", EventType.GOVERNANCE, EventLevel.IMPORTANT),

    /**
     * SpringBoot registration plugin grayscale configuration refresh event
     */
    SPRINGBOOT_GRAY_CONFIG_REFRESH("SPRINGBOOT_GRAY_CONFIG_REFRESH", EventType.OPERATION, EventLevel.NORMAL),

    /**
     * The SpringBoot service removes the registration event
     */
    SPRINGBOOT_UNREGISTRY("SPRINGBOOT_UNREGISTRY", EventType.GOVERNANCE,
            EventLevel.IMPORTANT);

    private final String name;

    private final EventType eventType;

    private final EventLevel eventLevel;

    SpringBootRegistryEventDefinition(String name, EventType eventType, EventLevel eventLevel) {
        this.name = name;
        this.eventType = eventType;
        this.eventLevel = eventLevel;
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

    /**
     * Obtain the scope
     *
     * @return string The name of the main module of the plugin
     */
    public String getScope() {
        return "springboot-registry-plugin";
    }
}
