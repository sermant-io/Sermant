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

/**
 * Spring boot registry plugin handler
 *
 * @author zhp
 * @since 2024-05-16
 */
public class SpringBootRegistryPluginHandler extends PluginConfigHandler {
    private static final String CONFIGURATION_NAME = "sermant.plugin.registry";

    @Override
    public ConfigInfo parsePluginInfo(String key, String group) {
        ConfigInfo configInfo = super.parsePluginInfo(key, group);
        if (StringUtils.isEmpty(configInfo.getServiceName())) {
            configInfo.setServiceName(CommonConst.GLOBAL_CONFIGURATION_SERVICE_NAME);
        }
        configInfo.setPluginType(PluginType.SPRINGBOOT_REGISTRY.getPluginName());
        return configInfo;
    }

    @Override
    public boolean verifyConfigurationGroup(String group) {
        if (StringUtils.isBlank(group)) {
            return false;
        }
        return group.matches(CommonConst.CONFIGURATION_DEFAULT_PATTERN);
    }

    @Override
    public boolean verifyConfigurationKey(String key) {
        if (StringUtils.isBlank(key)) {
            return false;
        }
        return key.equals(CONFIGURATION_NAME);
    }
}
