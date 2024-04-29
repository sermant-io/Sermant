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

package io.sermant.registry.config;

import io.sermant.core.common.LoggerFactory;
import io.sermant.core.service.dynamicconfig.common.DynamicConfigEvent;

import java.util.Locale;
import java.util.logging.Logger;

/**
 * In the dual-registration scenario, the heartbeat switch of the original registration center
 *
 * @author zhouss
 * @since 2022-05-24
 */
public class OriginRegistrySwitchConfigResolver extends RegistryConfigResolver {
    private static final Logger LOGGER = LoggerFactory.getLogger();

    private static final String ORIGIN_REGISTRY_SWITCH_PREFIX = "origin.__registry__.";

    private static final String REGISTRY_SWITCH_KEY = "sermant.agent.registry";

    private final RegisterDynamicConfig defaultConfig = new RegisterDynamicConfig();

    @Override
    protected String getConfigPrefix() {
        return ORIGIN_REGISTRY_SWITCH_PREFIX;
    }

    @Override
    protected Object getDefaultConfig() {
        return defaultConfig;
    }

    @Override
    protected Object getOriginConfig() {
        return RegisterDynamicConfig.INSTANCE;
    }

    @Override
    protected boolean isTargetConfig(DynamicConfigEvent event) {
        return REGISTRY_SWITCH_KEY.equals(event.getKey());
    }

    @Override
    protected void afterUpdateConfig() {
        final RegisterDynamicConfig originConfig = (RegisterDynamicConfig) getOriginConfig();
        LOGGER.info(String.format(Locale.ENGLISH, "Origin registry switch config update, new value: [needClose: %s]",
                originConfig.isNeedCloseOriginRegisterCenter()));
    }
}
