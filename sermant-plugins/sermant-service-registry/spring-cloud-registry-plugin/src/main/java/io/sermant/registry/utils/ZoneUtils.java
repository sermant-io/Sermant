/*
 * Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
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

package io.sermant.registry.utils;

import io.sermant.core.common.LoggerFactory;
import io.sermant.core.plugin.config.PluginConfigManager;
import io.sermant.registry.config.RegisterConfig;
import io.sermant.registry.config.SpringRegistryConstants;
import io.sermant.registry.context.RegisterContext;

import java.util.Locale;
import java.util.Map;

/**
 * Plug zone tool class
 *
 * @author zhouss
 * @since 2022-06-10
 */
public class ZoneUtils {
    private ZoneUtils() {
    }

    /**
     * Set the zone where the instance is registered
     *
     * @param meta Instance meta-information
     */
    public static void setZone(Map<String, String> meta) {
        final String originZone = meta.get(SpringRegistryConstants.LOAD_BALANCER_ZONE_META_KEY);
        if (originZone != null) {
            LoggerFactory.getLogger().info(String.format(Locale.ENGLISH, "Registry instance with zone [%s]",
                    originZone));
            return;
        }
        String zone = PluginConfigManager.getPluginConfig(RegisterConfig.class).getZone();
        if (zone == null) {
            zone = RegisterContext.INSTANCE.getClientInfo().getZone();
        }
        if (zone != null) {
            meta.put(SpringRegistryConstants.LOAD_BALANCER_ZONE_META_KEY, zone);
            LoggerFactory.getLogger().info(String.format(Locale.ENGLISH, "Registry instance with zone [%s]",
                    zone));
        }
    }
}
