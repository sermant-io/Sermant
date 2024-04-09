/*
 * Copyright (C) 2021-2024 Sermant Authors. All rights reserved.
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

import java.util.List;

/**
 * Perform operations on all keys in the same group
 *
 * @author HapThorin
 * @version 1.0.0
 * @since 2021-12-14
 */
public interface GroupService {
    /**
     * Gets all keys in the group
     *
     * @param group group
     * @return key list
     */
    List<String> listKeysFromGroup(String group);

    /**
     * Add listeners for all keys under the group
     *
     * @param group group
     * @param listener listener
     * @return add result
     */
    boolean addGroupListener(String group, DynamicConfigListener listener);

    /**
     * Removes listeners for all keys under the group
     *
     * @param group group
     * @return remove result
     */
    boolean removeGroupListener(String group);
}
