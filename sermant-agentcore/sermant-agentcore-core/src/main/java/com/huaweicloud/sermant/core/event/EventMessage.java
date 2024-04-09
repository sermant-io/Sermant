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

package com.huaweicloud.sermant.core.event;

import java.util.List;

/**
 * Event Message
 *
 * @author luanwenfei
 * @since 2023-03-07
 */
public class EventMessage {
    String metaHash;

    List<Event> events;

    /**
     * Constructor
     *
     * @param metaHash meta hash
     * @param events event list
     */
    public EventMessage(String metaHash, List<Event> events) {
        this.metaHash = metaHash;
        this.events = events;
    }

    public String getMetaHash() {
        return metaHash;
    }

    public void setMetaHash(String metaHash) {
        this.metaHash = metaHash;
    }

    public List<Event> getEvents() {
        return events;
    }

    public void setEvents(List<Event> events) {
        this.events = events;
    }

    @Override
    public String toString() {
        return "EventMessage{" + "metaHash='" + metaHash + '\'' + ", events=" + events + '}';
    }
}
