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

/**
 * Other plugin handler
 *
 * @author zhp
 * @since 2024-05-16
 */
public class OtherPluginHandler extends PluginConfigHandler {
    @Override
    public ConfigInfo parsePluginInfo(String key, String group) {
        ConfigInfo configInfo = new ConfigInfo();
        configInfo.setKey(key);
        configInfo.setGroup(group);
        configInfo.setPluginType(PluginType.OTHER.getPluginName());
        return configInfo;
    }

    @Override
    public boolean verifyConfigurationGroup(String group) {
        return StringUtils.isNotBlank(group);
    }

    @Override
    public boolean verifyConfigurationKey(String key) {
        return StringUtils.isNotBlank(key);
    }
}
