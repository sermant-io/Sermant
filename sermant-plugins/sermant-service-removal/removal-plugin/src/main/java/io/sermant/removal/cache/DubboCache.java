/*
 * Copyright (C) 2023-2023 Huawei Technologies Co., Ltd. All rights reserved.
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

package io.sermant.removal.cache;

import java.util.HashMap;
import java.util.Map;

/**
 * Dubbo service name and interface map cache class
 *
 * @author zhp
 * @since 2023-03-17
 */
public class DubboCache {
    /**
     * Caching MAP
     */
    private static final Map<String, String> SERVICE_CACHE = new HashMap<>();

    private DubboCache() {
    }

    /**
     * The relationship between the cache interface and the service name
     *
     * @param interfaceName The name of the interface
     * @param serviceName Service name
     */
    public static void putService(String interfaceName, String serviceName) {
        SERVICE_CACHE.put(interfaceName, serviceName);
    }

    /**
     * Obtain the app name
     *
     * @param serviceInterface Interface
     * @return The name of the app
     */
    public static String getServiceName(String serviceInterface) {
        return SERVICE_CACHE.get(serviceInterface);
    }
}
