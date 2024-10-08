/*
 * Copyright (C) 2021-2022 Huawei Technologies Co., Ltd. All rights reserved.
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

package io.sermant.router.spring.service;

import io.sermant.core.plugin.service.PluginService;

import java.util.Set;

/**
 * Configuration service
 *
 * @author provenceee
 * @since 2021-11-24
 */
public interface SpringConfigService extends PluginService {
    /**
     * Initialize the notification
     *
     * @param cacheName Cache name
     * @param serviceName Service name
     */
    void init(String cacheName, String serviceName);

    /**
     * Obtain the rule key
     *
     * @return Rule key
     */
    Set<String> getMatchKeys();

    /**
     * Obtain the staining key
     *
     * @return The key of the staining
     */
    Set<String> getInjectTags();
}
