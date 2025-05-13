/*
 * Copyright (C) 2024-2024 Sermant Authors. All rights reserved.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package io.sermant.mq.grayscale.config;

import io.sermant.core.plugin.config.PluginConfigManager;
import io.sermant.core.service.dynamicconfig.common.DynamicConfigEventType;
import io.sermant.mq.grayscale.config.rocketmq.RocketMqConfigUtils;

/**
 * grayscale config cache
 *
 * @author chengyouling
 * @since 2024-09-12
 **/
public class MqGrayConfigCache {
    private static MqGrayscaleConfig cacheConfig = PluginConfigManager.getPluginConfig(MqGrayscaleConfig.class);

    private MqGrayConfigCache() {
    }

    /**
     * get cache mqGrayscaleConfig
     *
     * @return mqGrayscaleConfig
     */
    public static MqGrayscaleConfig getCacheConfig() {
        return cacheConfig;
    }

    /**
     * set cache mqGrayscaleConfig
     *
     * @param config mqGrayscaleConfig
     * @param eventType eventType
     */
    public static void setCacheConfig(MqGrayscaleConfig config, DynamicConfigEventType eventType) {
        RocketMqConfigUtils.recordTrafficTagsSet(config);
        RocketMqConfigUtils.updateChangeFlag();
        if (eventType == DynamicConfigEventType.CREATE || eventType == DynamicConfigEventType.INIT) {
            cacheConfig = config;
            return;
        }
        cacheConfig.updateGrayscaleConfig(config);
    }

    /**
     * clear cache mqGrayscaleConfig
     */
    public static void clearCacheConfig() {
        cacheConfig = new MqGrayscaleConfig();
        RocketMqConfigUtils.updateChangeFlag();
    }
}
