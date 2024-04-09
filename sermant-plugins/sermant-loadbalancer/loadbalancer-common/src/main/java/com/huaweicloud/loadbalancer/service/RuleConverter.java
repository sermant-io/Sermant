/*
 * Copyright (C) 2022-2024 Sermant Authors. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.huaweicloud.loadbalancer.service;

import com.huaweicloud.sermant.core.plugin.service.PluginService;

import java.util.Optional;

/**
 * rule converter
 *
 * @author zhouss
 * @since 2022-08-09
 */
public interface RuleConverter extends PluginService {
    /**
     * convert a string to a load balancing rule
     *
     * @param rawContent configuration content
     * @param clazz target type
     * @param <T> target type
     * @return Loadbalancer rule
     */
    <T> Optional<T> convert(String rawContent, Class<T> clazz);
}
