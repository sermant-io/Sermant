/*
 * Copyright (C) 2021-2021 Huawei Technologies Co., Ltd. All rights reserved.
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

package io.sermant.core.service.dynamicconfig.common;

import java.util.EventObject;
import java.util.Objects;

/**
 * An event raised when the config changed, immutable.
 *
 * @author yangyshdan, HapThorin
 * @version 1.0.0
 * @see DynamicConfigEventType
 * @since 2021-12-27
 */
public class DynamicConfigEvent extends EventObject {
    private static final long serialVersionUID = 8199411666187944757L;

    private final String key;

    private final String group;

    private final String content;

    private final DynamicConfigEventType eventType;

    /**
     * constructor
     *
     * @param key key
     * @param group group
     * @param content config content
     * @param eventType event type
     */
    public DynamicConfigEvent(String key, String group, String content, DynamicConfigEventType eventType) {
        super(key + "," + group);
        this.key = key;
        this.group = group;
        this.content = content;
        this.eventType = eventType;
    }

    public String getKey() {
        return key;
    }

    public String getGroup() {
        return group;
    }

    public String getContent() {
        return content;
    }

    public DynamicConfigEventType getEventType() {
        return eventType;
    }

    @Override
    public String toString() {
        return "DynamicConfigEvent{" + "key='" + key + '\'' + ", group='" + group + '\'' + ", content='" + content
                + '\'' + ", eventType=" + eventType + "} " + super.toString();
    }

    @Override
    public boolean equals(Object target) {
        if (this == target) {
            return true;
        }
        if (!(target instanceof DynamicConfigEvent)) {
            return false;
        }
        DynamicConfigEvent that = (DynamicConfigEvent) target;
        return Objects.equals(getKey(), that.getKey()) && Objects.equals(getGroup(), that.getGroup())
                && Objects.equals(getContent(), that.getContent()) && getEventType() == that.getEventType();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getKey(), getGroup(), getContent(), getEventType());
    }

    /**
     * Build an initial dynamic configuration event
     *
     * @param key key
     * @param group group
     * @param content config content
     * @return dynamic configuration event
     */
    public static DynamicConfigEvent initEvent(String key, String group, String content) {
        return new DynamicConfigEvent(key, group, content, DynamicConfigEventType.INIT);
    }

    /**
     * Build create configuration event
     *
     * @param key key
     * @param group group
     * @param content config content
     * @return dynamic configuration event
     */
    public static DynamicConfigEvent createEvent(String key, String group, String content) {
        return new DynamicConfigEvent(key, group, content, DynamicConfigEventType.CREATE);
    }

    /**
     * Build modify configuration events
     *
     * @param key key
     * @param group group
     * @param content config content
     * @return dynamic configuration event
     */
    public static DynamicConfigEvent modifyEvent(String key, String group, String content) {
        return new DynamicConfigEvent(key, group, content, DynamicConfigEventType.MODIFY);
    }

    /**
     * Build delete configuration event
     *
     * @param key key
     * @param group group
     * @param content config content
     * @return dynamic configuration event
     */
    public static DynamicConfigEvent deleteEvent(String key, String group, String content) {
        return new DynamicConfigEvent(key, group, content, DynamicConfigEventType.DELETE);
    }
}
