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

package com.huaweicloud.sermant.core.service.dynamicconfig.api;

import com.huaweicloud.sermant.core.service.dynamicconfig.common.DynamicConfigListener;

/**
 * Perform operations on a key in the same group
 *
 * @author HapThorin
 * @version 1.0.0
 * @since 2021-12-14
 */
public interface KeyGroupService {
    /**
     * Get the configuration value for a key under the group
     *
     * @param key key
     * @param group group
     * @return configuration value
     */
    String getConfig(String key, String group);

    /**
     * Set the configuration value of a key under the group
     *
     * @param key key
     * @param group group
     * @param content config content
     * @return publish result
     */
    boolean publishConfig(String key, String group, String content);

    /**
     * Remove the configuration value of a key under the group
     *
     * @param key key
     * @param group group
     * @return remove result
     */
    boolean removeConfig(String key, String group);

    /**
     * Adds a listener for a key under the group
     *
     * @param key key
     * @param group group
     * @param listener listener
     * @return add result
     */
    boolean addConfigListener(String key, String group, DynamicConfigListener listener);

    /**
     * Remove the listener for a key under the group
     *
     * @param key key
     * @param group group
     * @return remove result
     */
    boolean removeConfigListener(String key, String group);
}
