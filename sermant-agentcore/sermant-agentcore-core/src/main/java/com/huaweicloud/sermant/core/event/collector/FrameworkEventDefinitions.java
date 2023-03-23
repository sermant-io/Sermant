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

package com.huaweicloud.sermant.core.event.collector;

import com.huaweicloud.sermant.core.event.EventLevel;
import com.huaweicloud.sermant.core.event.EventType;

/**
 * 框架事件的事件定义
 *
 * @author luanwenfei
 * @since 2023-03-04
 */
public enum FrameworkEventDefinitions {
    /**
     * Sermant启动事件信息定义
     */
    SERMANT_START("SERMANT_START", EventType.OPERATION, EventLevel.NORMAL),

    /**
     * Sermant字节码转换成功事件信息定义
     */
    SERMANT_TRANSFORM_SUCCESS("SERMANT_TRANSFORM_SUCCESS", EventType.OPERATION, EventLevel.NORMAL),

    /**
     * Sermant字节码转换失败事件信息定义
     */
    SERMANT_TRANSFORM_FAILURE("SERMANT_TRANSFORM_FAILURE", EventType.OPERATION, EventLevel.NORMAL),

    /**
     * Sermant服务启动事件信息定义
     */
    SERMANT_SERVICE_START("SERMANT_SERVICE_START", EventType.OPERATION, EventLevel.NORMAL),

    /**
     * Sermant服务停止事件信息定义
     */
    SERMANT_SERVICE_STOP("SERMANT_SERVICE_STOP", EventType.OPERATION, EventLevel.NORMAL),

    /**
     * Sermant插件加载事件信息定义
     */
    SERMANT_PLUGIN_LOAD("SERMANT_PLUGIN_LOAD", EventType.OPERATION, EventLevel.NORMAL),

    /**
     * Sermant停止事件信息定义
     */
    SERMANT_STOP("SERMANT_STOP", EventType.OPERATION, EventLevel.NORMAL);

    /**
     * 事件
     */
    private final String name;

    private final EventType eventType;

    private final EventLevel eventLevel;

    FrameworkEventDefinitions(String name, EventType eventType, EventLevel eventLevel) {
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

    public String getScope() {
        return "framework";
    }
}
