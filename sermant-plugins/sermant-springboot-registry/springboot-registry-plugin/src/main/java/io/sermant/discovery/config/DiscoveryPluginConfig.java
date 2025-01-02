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

package io.sermant.discovery.config;

import io.sermant.core.config.common.ConfigTypeKey;
import io.sermant.core.plugin.config.PluginConfig;

/**
 * Configure configurations such as domain names and log printing
 *
 * @author chengyouling
 * @since 2022-10-10
 */
@ConfigTypeKey("sermant.springboot.registry")
public class DiscoveryPluginConfig implements PluginConfig {
    /**
     * Blocked domains
     */
    private String realmName;

    /**
     * Whether to print statistics logs
     */
    private boolean enableRequestCount = false;

    /**
     * Specifies whether to enable boot registration
     */
    private boolean enableRegistry = true;

    /**
     * Registry type, currently supports NACOS and ZOOKEEPER;
     */
    private RegisterType registerType = RegisterType.ZOOKEEPER;

    public boolean isEnableRegistry() {
        return enableRegistry;
    }

    public void setEnableRegistry(boolean enableRegistry) {
        this.enableRegistry = enableRegistry;
    }

    public String getRealmName() {
        return realmName;
    }

    public void setRealmName(String realmName) {
        this.realmName = realmName;
    }

    public boolean isEnableRequestCount() {
        return enableRequestCount;
    }

    public void setEnableRequestCount(boolean enableRequestCount) {
        this.enableRequestCount = enableRequestCount;
    }

    public RegisterType getRegisterType() {
        return registerType;
    }

    public void setRegisterType(RegisterType registerType) {
        this.registerType = registerType;
    }
}
