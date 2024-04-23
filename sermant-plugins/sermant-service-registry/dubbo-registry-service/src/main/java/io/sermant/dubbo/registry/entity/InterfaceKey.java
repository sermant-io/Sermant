/*
 * Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
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

package io.sermant.dubbo.registry.entity;

import java.util.Objects;

/**
 * The key of the dubbo interface
 *
 * @author provenceee
 * @since 2022-04-06
 */
public class InterfaceKey {
    private String group;

    private String version;

    /**
     * Constructor
     */
    public InterfaceKey() {
    }

    /**
     * Constructor
     *
     * @param group Group
     * @param version Version
     */
    public InterfaceKey(String group, String version) {
        this.group = group;
        this.version = version;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj != null && getClass() == obj.getClass()) {
            InterfaceKey that = (InterfaceKey) obj;
            return Objects.equals(group, that.group) && Objects.equals(version, that.version);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(group, version);
    }
}