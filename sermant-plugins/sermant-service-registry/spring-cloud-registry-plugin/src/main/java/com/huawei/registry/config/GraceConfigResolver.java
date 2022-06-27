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

package com.huawei.registry.config;

import com.huaweicloud.sermant.core.common.LoggerFactory;
import com.huaweicloud.sermant.core.plugin.config.PluginConfigManager;
import com.huaweicloud.sermant.core.service.dynamicconfig.common.DynamicConfigEvent;

import java.util.Locale;
import java.util.logging.Logger;

/**
 * 优雅上下线配置解析
 *
 * @author zhouss
 * @since 2022-05-24
 */
public class GraceConfigResolver extends RegistryConfigResolver {
    private static final Logger LOGGER = LoggerFactory.getLogger();

    /**
     * 优雅上下线配置前缀
     */
    private static final String GRACE_CONFIG_PREFIX = "rule.";

    /**
     * 优雅上下线配置KEY
     */
    private static final String GRACE_CONFIG_KEY_NAME = "sermant.agent.grace";

    private GraceConfig defaultConfig = null;

    @Override
    protected String getConfigPrefix() {
        return GRACE_CONFIG_PREFIX;
    }

    @Override
    protected GraceConfig getDefaultConfig() {
        if (defaultConfig == null) {
            defaultConfig = getOriginConfig().clone();
        }
        return defaultConfig;
    }

    @Override
    protected GraceConfig getOriginConfig() {
        return PluginConfigManager.getPluginConfig(GraceConfig.class);
    }

    @Override
    protected boolean isTargetConfig(DynamicConfigEvent event) {
        return GRACE_CONFIG_KEY_NAME.equals(event.getKey());
    }

    @Override
    protected void afterUpdateConfig() {
        LOGGER.info(String.format(Locale.ENGLISH, "GraceConfig update, new value: [%s]", getOriginConfig().toString()));
    }
}
