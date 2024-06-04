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

package io.sermant.backend.handler.config;

import io.sermant.backend.entity.config.ConfigInfo;

import java.util.HashMap;
import java.util.Map;

/**
 * plugin Handler
 *
 * @author zhp
 * @since 2024-05-16
 */
public abstract class PluginConfigHandler {
    /**
     * The key for the application name
     */
    protected static final String APP_KEY = "app";

    /**
     * The key for the environment
     */
    protected static final String ENVIRONMENT_KEY = "environment";

    /**
     * The key for the service name
     */
    protected static final String SERVICE_KEY = "service";

    /**
     * The key for the zone name
     */
    protected static final String ZONE_KEY = "zone";

    private static final String GROUP_SPLIT = "&";

    private static final String GROUP_CONNECT = "=";

    /**
     * Parse plugin information, extract application name, service name, and environment name from group and key
     * information
     *
     * @param key Configuration Item Name
     * @param group Configuration item group name
     * @return configuration information
     */
    public ConfigInfo parsePluginInfo(String key, String group) {
        Map<String, String> map = parseGroup(group);
        ConfigInfo configInfo = new ConfigInfo();
        configInfo.setAppName(map.get(APP_KEY));
        configInfo.setEnvironment(map.get(ENVIRONMENT_KEY));
        configInfo.setServiceName(map.get(SERVICE_KEY));
        configInfo.setKey(key);
        configInfo.setGroup(group);
        return configInfo;
    }

    /**
     * Verify if the configuration item is the current plugin configuration
     *
     * @param key Configuration Item Name
     * @param group Configuration item group name
     * @return Verification results
     */
    public abstract boolean verifyConfiguration(String key, String group);

    /**
     * Parsing group information
     *
     * @param group Configuration item group name
     * @return Parsed information
     */
    Map<String, String> parseGroup(String group) {
        Map<String, String> map = new HashMap<>();
        String[] parts = group.split(GROUP_SPLIT);
        for (String part : parts) {
            String[] keyValue = part.split(GROUP_CONNECT);
            if (keyValue.length > 1) {
                map.put(keyValue[0], keyValue[1]);
            }
        }
        return map;
    }
}
