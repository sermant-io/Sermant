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
 * Tag Transmission plugin handler
 *
 * @author zhp
 * @since 2024-05-16
 */
public class TagTransmissionPluginHandler extends PluginConfigHandler {
    private static final String CONFIGURATION_GROUP_NAME = "sermant/tag-transmission-plugin";

    private static final String CONFIGURATION_KEY_NAME = "tag-config";

    @Override
    public ConfigInfo parsePluginInfo(String key, String group) {
        ConfigInfo configInfo = new ConfigInfo();
        configInfo.setKey(key);
        configInfo.setGroup(group);
        configInfo.setServiceName(CommonConst.GLOBAL_CONFIGURATION_SERVICE_NAME);
        configInfo.setPluginType(PluginType.TAG_TRANSMISSION.getPluginName());
        return configInfo;
    }

    @Override
    public boolean verifyConfigurationGroup(String group) {
        if (StringUtils.isBlank(group)) {
            return false;
        }
        return group.equals(CONFIGURATION_GROUP_NAME);
    }

    @Override
    public boolean verifyConfigurationKey(String key) {
        if (StringUtils.isBlank(key)) {
            return false;
        }
        return key.equals(CONFIGURATION_KEY_NAME);
    }
}
