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

package com.huaweicloud.sermant.core.event.common;

/**
 * 事件种类
 *
 * @author luanwenfei
 * @since 2023-03-04
 */
public enum EventType {
    /**
     * 运行事件
     */
    OPERATION(0, "operation"),

    /**
     * 治理事件
     */
    GOVERNANCE(1, "governance"),

    /**
     * 日志事件
     */
    LOG(2, "log");

    /**
     * 事件种类的整型标识
     */
    private final int type;

    /**
     * 事件种类描述
     */
    private final String description;

    EventType(int type, String description) {
        this.type = type;
        this.description = description;
    }

    public int getType() {
        return type;
    }

    public String getDescription() {
        return description;
    }
}
