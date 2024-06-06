/*
 * Copyright (C) 2024-2024 Sermant Authors. All rights reserved.
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

package io.sermant.implement.service.dynamicconfig;

import java.util.List;
import java.util.Map;

/**
 * Configuration Center Client Interface
 *
 * @author zhp
 * @since 2024-05-16
 */
public interface ConfigClient {
    /**
     * Get configuration content
     *
     * @param key configuration key
     * @param group configuration group
     * @return configuration content
     */
    String getConfig(String key, String group);

    /**
     * According to the key and group query configuration list, supports fuzzy queries and precise matching queries
     *
     * @param key configuration key
     * @param group configuration group
     * @param exactMatchFlag Identification of exact match
     * @return configuration content
     */
    Map<String, List<String>> getConfigList(String key, String group, boolean exactMatchFlag);

    /**
     * Publish configuration
     *
     * @param key configuration key
     * @param group configuration group
     * @param content configuration content
     * @return publish result
     */
    boolean publishConfig(String key, String group, String content);

    /**
     * Delete configuration
     *
     * @param key configuration key
     * @param group configuration group
     * @return remove result
     */
    boolean removeConfig(String key, String group);

    /**
     * Determine if the connection is successful
     *
     * @return if the connection is successful
     */
    boolean isConnect();
}
