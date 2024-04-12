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

package com.huawei.discovery.config;

/**
 * Constants related to the dynamic configuration of plugin effect and log printing
 *
 * @author chengyouling
 * @since 2022-10-10
 */
public class PlugEffectWhiteBlackConstants {

    /**
     * Listen for the configuration key
     */
    public static final String DYNAMIC_CONFIG_LISTENER_KEY = "sermant.plugin.registry";

    /**
     * Plugin Takes Effect - Strategy
     */
    public static final String DYNAMIC_CONFIG_STRATEGY = "strategy";

    /**
     * Plugin Validity--Service Name Whitelist Value
     */
    public static final String DYNAMIC_CONFIG_VALUE = "value";

    /**
     * Policy - All services take effect
     */
    public static final String STRATEGY_ALL = "all";

    /**
     * Policy - All services are not in effect
     */
    public static final String STRATEGY_NONE = "none";

    /**
     * Policy-Whitelist takes effect
     */
    public static final String STRATEGY_WHITE = "white";

    /**
     * The policy - blacklist service does not take effect
     */
    public static final String STRATEGY_BLACK = "black";

    private PlugEffectWhiteBlackConstants() {

    }
}
