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

package com.huawei.discovery.utils;

import com.huawei.discovery.config.DiscoveryPluginConfig;
import com.huawei.discovery.config.PlugEffectWhiteBlackConstants;
import com.huawei.discovery.entity.PlugEffectStrategyCache;

import com.huaweicloud.sermant.core.plugin.config.PluginConfigManager;
import com.huaweicloud.sermant.core.utils.StringUtils;

import java.util.Set;

/**
 * 插件生效、日志打印动态配置相关工具类
 *
 * @author chengyouling
 * @since 2022-10-09
 */
public class PlugEffectWhiteBlackUtils {
    private static final String COMMA = ",";

    /**
     * 域名列表
     */
    private static String[] domainNames;

    private PlugEffectWhiteBlackUtils() {
    }

    /**
     * 判断对应服务插件是否执行
     *
     * @param serviceName 服务名
     * @return 是否生效
     */
    public static boolean isPlugEffect(String serviceName) {
        String strategy = PlugEffectStrategyCache.INSTANCE.getConfigContent(
                PlugEffectWhiteBlackConstants.DYNAMIC_CONFIG_STRATEGY);

        // 全部生效
        if (StringUtils.equalsIgnoreCase(PlugEffectWhiteBlackConstants.STRATEGY_ALL, strategy)) {
            return true;
        }

        // 全部不生效
        if (StringUtils.equalsIgnoreCase(PlugEffectWhiteBlackConstants.STRATEGY_NONE, strategy)) {
            return false;
        }

        final Set<String> curServices = PlugEffectStrategyCache.INSTANCE.getCurServices();

        // 白名单-插件生效
        if (StringUtils.equalsIgnoreCase(PlugEffectWhiteBlackConstants.STRATEGY_WHITE, strategy)) {
            return curServices.contains(serviceName);
        }

        // 黑名单-插件不生效
        if (StringUtils.equalsIgnoreCase(PlugEffectWhiteBlackConstants.STRATEGY_BLACK, strategy)) {
            return !curServices.contains(serviceName);
        }
        return false;
    }

    /**
     * 判断url是否包含指定域名
     *
     * @param url 拦截获取的域名
     * @return 是否包含域名
     */
    public static boolean isUrlContainsRealmName(String url) {
        final String[] names = getDomainNames();
        for (String name : names) {
            if (url.contains(name)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 判断主机名称是否为设置的域名
     *
     * @param host 拦截获取的域名
     * @return 是否等于域名
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
                    domainNames = new String[parts.length];
                    for (int i = 0; i < parts.length; i++) {
                        domainNames[i] = parts[i].trim();
                    }
                }
            }
        }
        return domainNames;
    }

    /**
     * 判断是否允许请求通过插件
     *
     * @param realmStr 拦截获取的域名
     * @param serviceName 下游服务名
     * @return 是否允许执行
     */
    public static boolean isAllowRun(String realmStr, String serviceName) {
        if (!PlugEffectWhiteBlackUtils.isHostEqualRealmName(realmStr)) {
            return false;
        }
        return PlugEffectWhiteBlackUtils.isPlugEffect(serviceName);
    }
}
