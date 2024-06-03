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

import io.sermant.backend.common.conf.CommonConst;
import io.sermant.backend.entity.config.ConfigInfo;
import io.sermant.backend.entity.config.PluginType;

import org.apache.commons.lang.StringUtils;

import java.util.Map;

/**
 * Router plugin handler
 *
 * @author zhp
 * @since 2024-05-16
 */
public class RouterPluginHandler extends PluginConfigHandler {
    private static final String GLOBAL_CONFIGURATION_NAME = "servicecomb.globalRouteRule";

    private static final String SERVICE_CONFIGURATION_NAME_PREFIX = "servicecomb.routeRule.";

    @Override
    public ConfigInfo parsePluginInfo(String key, String group) {
        Map<String, String> map = parseGroup(group);
        ConfigInfo configInfo = new ConfigInfo();
        configInfo.setAppName(map.get(APP_KEY));
        configInfo.setEnvironment(map.get(ENVIRONMENT_KEY));
        if (StringUtils.equals(key, GLOBAL_CONFIGURATION_NAME)) {
            configInfo.setServiceName(CommonConst.GLOBAL_CONFIGURATION_SERVICE_NAME);
        } else if (key.startsWith(SERVICE_CONFIGURATION_NAME_PREFIX)) {
            configInfo.setServiceName(key.replace(SERVICE_CONFIGURATION_NAME_PREFIX, ""));
        }
        configInfo.setKey(key);
        configInfo.setGroup(group);
        configInfo.setPluginType(PluginType.ROUTER.getPluginName());
        return configInfo;
    }

    @Override
    public boolean verifyConfiguration(String key, String group) {
        if (StringUtils.isBlank(key) || StringUtils.isBlank(group)) {
            return false;
        }
        if (!group.matches(CommonConst.CONFIGURATION_DEFAULT_PATTERN)) {
            return false;
        }
        return key.equals(GLOBAL_CONFIGURATION_NAME) || key.startsWith(SERVICE_CONFIGURATION_NAME_PREFIX);
    }
}
