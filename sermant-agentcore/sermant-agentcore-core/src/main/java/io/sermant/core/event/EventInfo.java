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

import java.util.Objects;

/**
 * Event information
 *
 * @author luanwenfei
 * @since 2023-03-02
 */
public class EventInfo {
    private String name;

    private String description;

    /**
     * constructor
     *
     * @param name event name
     * @param description event description
     */
    public EventInfo(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "EventInfo{" + "name='" + name + '\'' + ", description='" + description + '\'' + '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        EventInfo eventInfo = (EventInfo) obj;
        return Objects.equals(name, eventInfo.name) && Objects.equals(description,
                eventInfo.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, description);
    }
}
