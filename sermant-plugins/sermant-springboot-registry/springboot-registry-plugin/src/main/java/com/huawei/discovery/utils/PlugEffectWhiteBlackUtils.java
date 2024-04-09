/*
 * Copyright (C) 2022-2024 Sermant Authors. All rights reserved.
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

package com.huawei.discovery.utils;

import com.huawei.discovery.config.DiscoveryPluginConfig;
import com.huawei.discovery.config.PlugEffectWhiteBlackConstants;
import com.huawei.discovery.entity.PlugEffectStrategyCache;

import com.huaweicloud.sermant.core.plugin.config.PluginConfigManager;
import com.huaweicloud.sermant.core.utils.StringUtils;

import java.util.Set;

/**
 * plugin effect, log printing, dynamic configuration of related tools
 *
 * @author chengyouling
 * @since 2022-10-09
 */
public class PlugEffectWhiteBlackUtils {
    private static final String COMMA = ",";

    /**
     * List of domain names
     */
    private static volatile String[] domainNames;

    private PlugEffectWhiteBlackUtils() {
    }

    /**
     * Check whether the corresponding service plugin is executed
     *
     * @param serviceName Service name
     * @return Whether it is in effect
     */
    public static boolean isPlugEffect(String serviceName) {
        String strategy = PlugEffectStrategyCache.INSTANCE.getConfigContent(
                PlugEffectWhiteBlackConstants.DYNAMIC_CONFIG_STRATEGY);

        // All in effect
        if (StringUtils.equalsIgnoreCase(PlugEffectWhiteBlackConstants.STRATEGY_ALL, strategy)) {
            return true;
        }

        // None of them take effect
        if (StringUtils.equalsIgnoreCase(PlugEffectWhiteBlackConstants.STRATEGY_NONE, strategy)) {
            return false;
        }

        final Set<String> curServices = PlugEffectStrategyCache.INSTANCE.getCurServices();

        // Whitelist - The plugin takes effect
        if (StringUtils.equalsIgnoreCase(PlugEffectWhiteBlackConstants.STRATEGY_WHITE, strategy)) {
            return curServices.contains(serviceName);
        }

        // Blacklist - The plugin does not take effect
        if (StringUtils.equalsIgnoreCase(PlugEffectWhiteBlackConstants.STRATEGY_BLACK, strategy)) {
            return !curServices.contains(serviceName);
        }
        return false;
    }

    /**
     * Check whether the host name is the domain name that is set
     *
     * @param host Intercept the obtained domain name
     * @return Whether it is equal to the domain name
     */
    public static boolean isHostEqualRealmName(String host) {
        final String[] names = getDomainNames();
        for (String name : names) {
            if (StringUtils.equalsIgnoreCase(host, name)) {
                return true;
            }
        }
        return false;
    }

    private static String[] getDomainNames() {
        if (domainNames != null) {
            return domainNames;
        }
        synchronized (PlugEffectWhiteBlackUtils.class) {
            if (domainNames == null) {
                final String realmName = PluginConfigManager.getPluginConfig(DiscoveryPluginConfig.class)
                        .getRealmName();
                if (StringUtils.isEmpty(realmName)) {
                    domainNames = new String[0];
                } else {
                    final String[] parts = realmName.split(COMMA);
                    String[] arr = new String[parts.length];
                    for (int i = 0; i < parts.length; i++) {
                        arr[i] = parts[i].trim();
                    }
                    domainNames = arr;
                }
            }
        }
        return domainNames;
    }

    /**
     * Determine whether to allow the request to pass through the plugin
     *
     * @param realmStr Intercept the obtained domain name
     * @param serviceName Downstream service name
     * @return Whether or not to allow execution
     */
    public static boolean isAllowRun(String realmStr, String serviceName) {
        if (!PlugEffectWhiteBlackUtils.isHostEqualRealmName(realmStr)) {
            return false;
        }
        return PlugEffectWhiteBlackUtils.isPlugEffect(serviceName);
    }
}
