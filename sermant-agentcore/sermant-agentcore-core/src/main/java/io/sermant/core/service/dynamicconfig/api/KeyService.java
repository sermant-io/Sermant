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

package io.sermant.core.service.dynamicconfig.api;

import io.sermant.core.service.dynamicconfig.common.DynamicConfigListener;

import java.util.List;

/**
 * Perform operations on a key
 *
 * @author HapThorin
 * @version 1.0.0
 * @since 2021-12-14
 */
public interface KeyService {
    /**
     * Get the configuration value for a key
     *
     * @param key key
     * @return config value
     */
    String getConfig(String key);

    /**
     * Set the configuration value of a key
     *
     * @param key key
     * @param content content
     * @return publish result
     */
    boolean publishConfig(String key, String content);

    /**
     * Remove the configuration value of a key
     *
     * @param key key
     * @return remove result
     */
    boolean removeConfig(String key);

    /**
     * Get all keys
     *
     * @return key list
     */
    List<String> listKeys();

    /**
     * Add a listener for a key
     *
     * @param key key
     * @param listener listener
     * @return add result
     */
    boolean addConfigListener(String key, DynamicConfigListener listener);

    /**
     * Remove the listener for a key
     *
     * @param key key
     * @return remove result
     */
    boolean removeConfigListener(String key);
}
