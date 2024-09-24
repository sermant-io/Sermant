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
        if (eventType == DynamicConfigEventType.CREATE) {
            cacheConfig = config;
            RocketMqConfigUtils.updateChangeFlag();
            RocketMqConfigUtils.recordTrafficTagsSet(config);
            return;
        }
        boolean isAllowRefresh = isAllowRefreshChangeFlag(cacheConfig, config);
        if (isAllowRefresh) {
            cacheConfig.updateGrayscaleConfig(config);
            RocketMqConfigUtils.updateChangeFlag();
            RocketMqConfigUtils.recordTrafficTagsSet(config);
        }
    }

    /**
     * clear cache mqGrayscaleConfig
     */
    public static void clearCacheConfig() {
        cacheConfig = new MqGrayscaleConfig();
        RocketMqConfigUtils.updateChangeFlag();
    }

    /**
     * only traffic label changes allow refresh tag change map to rebuild SQL92 query statement,
     * because if the serviceMeta changed, the gray consumer cannot be matched and becomes a base consumer
     * so, if you need to change the env tag, restart all services.
     *
     * @param resource cache config
     * @param target cache config
     * @return boolean
     */
    private static boolean isAllowRefreshChangeFlag(MqGrayscaleConfig resource, MqGrayscaleConfig target) {
        if (resource.isEnabled() != target.isEnabled()) {
            return true;
        }
        if (resource.isBaseExcludeGroupTagsChanged(target)) {
            return true;
        }
        if (resource.isConsumerModeChanged(target)) {
            return true;
        }
        return !resource.buildAllTrafficTagInfoToStr().equals(target.buildAllTrafficTagInfoToStr());
    }
}
