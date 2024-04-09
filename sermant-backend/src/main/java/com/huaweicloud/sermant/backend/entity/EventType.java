/*
 * Copyright (C) 2023-2024 Sermant Authors. All rights reserved.
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

package com.huaweicloud.sermant.backend.entity;

/**
 * Event Type
 *
 * @author xuezechao
 * @since 2023-03-02
 */
public enum EventType {
    /**
     * OPERATION event
     */
    OPERATION(0, "operation"),

    /**
     * GOVERNANCE event
     */
    GOVERNANCE(1, "governance"),

    /**
     * LOG event
     */
    LOG(2, "log");

    private final int type;

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
