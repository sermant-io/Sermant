/*
 * Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package io.sermant.dynamic.config;

import io.sermant.core.plugin.config.PluginConfigManager;
import io.sermant.core.service.dynamicconfig.common.DynamicConfigEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * dynamic configuration
 *
 * @author zhouss
 * @since 2022-04-15
 */
public abstract class DynamicConfigSource implements ConfigSource {
    /**
     * dynamic configuration
     */
    protected final DynamicConfiguration configuration;

    /**
     * specify configuration key
     */
    private final List<String> sourceKeys = new ArrayList<>();

    /**
     * Dynamic configuration source initialization, parsing the configuration key specified by the configuration source
     */
    protected DynamicConfigSource() {
        configuration = PluginConfigManager.getPluginConfig(DynamicConfiguration.class);
        if (configuration.getSourceKeys() != null) {
            final String[] sources = configuration.getSourceKeys().split(",");
            for (String key : sources) {
                sourceKeys.add(key.trim());
            }
        }
    }

    /**
     * configuration event update
     *
     * @param event configuration event
     * @return whether to refresh the configuration
     */
    public final boolean accept(DynamicConfigEvent event) {
        // configure read condition 1、if no key is specified all is read
        // 2、If the configuration key is specified, only the specified key is read
        if (sourceKeys.isEmpty() || sourceKeys.contains(event.getKey())) {
            return doAccept(event);
        }
        return false;
    }

    /**
     * The underlying implementation configuration updates
     *
     * @param event notification event
     * @return return true if processing succeeds
     */
    protected abstract boolean doAccept(DynamicConfigEvent event);
}
