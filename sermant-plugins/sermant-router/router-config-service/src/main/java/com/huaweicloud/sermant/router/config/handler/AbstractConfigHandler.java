/*
 * Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huaweicloud.sermant.router.config.handler;

import com.huaweicloud.sermant.core.plugin.config.PluginConfigManager;
import com.huaweicloud.sermant.router.common.config.RouterConfig;
import com.huaweicloud.sermant.router.config.common.SafeConstructor;

import org.yaml.snakeyaml.Yaml;

/**
 * Configure the handler
 *
 * @author provenceee
 * @since 2022-08-09
 */
public abstract class AbstractConfigHandler implements AbstractHandler {
    private static final RouterConfig ROUTER_CONFIG = PluginConfigManager.getPluginConfig(RouterConfig.class);

    /**
     * yaml
     */
    protected final Yaml yaml;

    /**
     * Constructor
     */
    public AbstractConfigHandler() {
        this.yaml = new Yaml(new SafeConstructor(null));
    }

    /**
     * Whether it needs to be processed
     *
     * @param key configuration key
     * @return Whether it needs to be processed
     */
    @Override
    public boolean shouldHandle(String key) {
        // return negation value of compatibilityEnabled switch
        return !ROUTER_CONFIG.isEnabledPreviousRule();
    }
}