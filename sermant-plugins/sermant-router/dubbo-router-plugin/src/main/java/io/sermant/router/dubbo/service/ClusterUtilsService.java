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

package io.sermant.router.dubbo.service;

import io.sermant.core.plugin.service.PluginService;

/**
 * The service of ClusterUtils
 *
 * @author provenceee
 * @since 2022-03-09
 */
public interface ClusterUtilsService extends PluginService {
    /**
     * Cache the mapping relationship between the interface and downstream service names from the URL,
     * and remove label related parameters from the map
     *
     * @param arguments request parameters
     */
    void doBefore(Object[] arguments);
}