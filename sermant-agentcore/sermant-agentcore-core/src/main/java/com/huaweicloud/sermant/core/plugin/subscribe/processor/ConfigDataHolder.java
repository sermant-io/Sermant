/*
 * Copyright (C) 2022-2024 Sermant Authors. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.huaweicloud.sermant.core.plugin.subscribe.processor;

import java.util.HashMap;
import java.util.Map;

/**
 * Config data holder
 *
 * @author zhouss
 * @since 2022-04-21
 */
public class ConfigDataHolder implements Comparable<ConfigDataHolder> {
    /**
     * Order: smaller order means higher priority
     */
    private final int order;

    /**
     * All data currently held by the group
     * <p>
     * key: Configuration key value: All values of the configuration key
     * </p>
     */
    private final Map<String, Map<String, Object>> holder = new HashMap<>();

    private final String group;

    /**
     * Constructor
     *
     * @param group Group label
     * @param order order
     */
    public ConfigDataHolder(String group, int order) {
        this.group = group;
        this.order = order;
    }

    @Override
    public int compareTo(ConfigDataHolder target) {
        return Integer.compare(target.order, this.order);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }

    public String getGroup() {
        return group;
    }

    public Map<String, Map<String, Object>> getHolder() {
        return holder;
    }
}
