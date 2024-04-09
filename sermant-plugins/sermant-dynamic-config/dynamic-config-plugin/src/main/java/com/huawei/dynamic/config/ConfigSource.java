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

package com.huawei.dynamic.config;

import java.util.Set;

/**
 * configuration
 *
 * @author zhouss
 * @since 2022-04-15
 */
public interface ConfigSource extends Comparable<ConfigSource> {
    /**
     * priorityComparison
     *
     * @param target comparison object
     * @return priority
     */
    @Override
    default int compareTo(ConfigSource target) {
        if (target == null) {
            return -1;
        }
        return Integer.compare(this.order(), target.order());
    }

    /**
     * enable or not
     *
     * @return enable or not
     */
    default boolean isEnabled() {
        return true;
    }

    /**
     * gets all configuration names
     *
     * @return all configuration names
     */
    Set<String> getConfigNames();

    /**
     * get a single configuration
     *
     * @param key configuration key
     * @return configuration value
     */
    Object getConfig(String key);

    /**
     * Priority: The smaller the priority, the higher the priority
     *
     * @return priority
     */
    int order();
}
