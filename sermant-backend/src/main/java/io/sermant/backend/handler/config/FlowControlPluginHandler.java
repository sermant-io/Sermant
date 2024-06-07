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
import io.sermant.backend.entity.config.PluginType;

import org.apache.commons.lang.StringUtils;

import java.util.Map;

/**
 * Flow control plugin handler
 *
 * @author zhp
 * @since 2024-05-16
 */
public class FlowControlPluginHandler extends PluginConfigHandler {
    private static final String[] CONFIGURATION_NAME_PREFIX_ARRAY = {"servicecomb.rateLimiting.",
            "servicecomb.matchGroup.", "servicecomb.circuitBreaker.", "servicecomb.bulkhead.",
            "servicecomb.faultInjection.", "servicecomb.retry.", "servicecomb.system."};

    private static final String PATTERN = "^service=[^&]*$";

    @Override
    public ConfigInfo parsePluginInfo(String key, String group) {
        Map<String, String> map = parseGroup(group);
        ConfigInfo configInfo = new ConfigInfo();
        configInfo.setServiceName(map.get(SERVICE_KEY));
        configInfo.setKey(key);
        configInfo.setGroup(group);
        configInfo.setPluginType(PluginType.FLOW_CONTROL.getPluginName());
        return configInfo;
    }

    @Override
    public boolean verifyConfigurationGroup(String group) {
        if (StringUtils.isBlank(group)) {
            return false;
        }
        return group.matches(PATTERN);
    }

    @Override
    public boolean verifyConfigurationKey(String key) {
        if (StringUtils.isBlank(key)) {
            return false;
        }
        for (String name : CONFIGURATION_NAME_PREFIX_ARRAY) {
            if (key.startsWith(name)) {
                return true;
            }
        }
        return false;
    }
}
