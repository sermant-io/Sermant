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
 * Springboot注册插件事件定义
 *
 * @author lilai
 * @since 2023-04-14
 */
public enum SpringBootRegistryEventDefinition {
    /**
     * SpringBoot服务注册事件
     */
    SPRINGBOOT_REGISTRY("SPRINGBOOT_REGISTRY", EventType.GOVERNANCE, EventLevel.IMPORTANT),

    /**
     * SpringBoot注册插件灰度配置刷新事件
     */
    SPRINGBOOT_GRAY_CONFIG_REFRESH("SPRINGBOOT_GRAY_CONFIG_REFRESH", EventType.OPERATION, EventLevel.NORMAL),

    /**
     * SpringBoot服务移除注册事件
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
     * 获取scope
     *
     * @return string 插件主模块名称
     */
    public String getScope() {
        return "springboot-registry-plugin";
    }
}
