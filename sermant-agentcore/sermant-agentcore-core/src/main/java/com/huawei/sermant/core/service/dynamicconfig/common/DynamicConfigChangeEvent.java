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

package com.huawei.sermant.core.service.dynamicconfig.common;

import java.util.EventObject;
import java.util.Objects;

/**
 * An event raised when the config changed, immutable.
 *
 * @see DynamicConfigChangeType
 */
public class DynamicConfigChangeEvent extends EventObject {

    private final String key;

    private final String group;

    private final String content;

    private final DynamicConfigChangeType changeType;

    public DynamicConfigChangeEvent(String key, String group, String content, DynamicConfigChangeType changeType) {
        super(key + "," + group);
        this.key = key;
        this.group = group;
        this.content = content;
        this.changeType = changeType;
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

    public DynamicConfigChangeType getChangeType() {
        return changeType;
    }

    @Override
    public String toString() {
        return "ConfigChangedEvent{" +
                "key='" + key + '\'' +
                ", group='" + group + '\'' +
                ", content='" + content + '\'' +
                ", changeType=" + changeType +
                "} " + super.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof DynamicConfigChangeEvent)) {
            return false;
        }
        DynamicConfigChangeEvent that = (DynamicConfigChangeEvent) o;
        return Objects.equals(getKey(), that.getKey()) &&
                Objects.equals(getGroup(), that.getGroup()) &&
                Objects.equals(getContent(), that.getContent()) &&
                getChangeType() == that.getChangeType();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getKey(), getGroup(), getContent(), getChangeType());
    }
}
