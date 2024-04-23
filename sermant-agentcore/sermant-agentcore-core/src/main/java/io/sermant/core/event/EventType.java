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

package io.sermant.core.event;

/**
 * Event Type
 *
 * @author luanwenfei
 * @since 2023-03-04
 */
public enum EventType {
    /**
     * operation event
     */
    OPERATION(0, "operation"),

    /**
     * governance event
     */
    GOVERNANCE(1, "governance"),

    /**
     * log event
     */
    LOG(2, "log");

    /**
     * Integer identifier of the event type
     */
    private final int type;

    /**
     * event type description
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
