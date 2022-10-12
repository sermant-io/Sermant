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

import java.net.URI;
import java.util.Map;

import com.huawei.discovery.config.DiscoveryPluginConfig;
import com.huawei.discovery.config.PlugEffectWhiteBlackConstants;
import com.huawei.discovery.entity.PlugEffectStategyCache;
import com.huaweicloud.sermant.core.plugin.config.PluginConfigManager;
import com.huaweicloud.sermant.core.utils.StringUtils;

/**
 * 插件生效、日志打印动态配置相关工具类
 *
 * @author chengyouling
 * @since 2022-10-09
 */
public class PlugEffectWhiteBlackUtils {

    private static DiscoveryPluginConfig config = PluginConfigManager.getPluginConfig(DiscoveryPluginConfig.class);

    /**
     * 判断对应服务插件是否执行
     * @param serviceName
     * @return
     */
    public static boolean isPlugEffect(String serviceName) {
        String strategy = PlugEffectStategyCache.INSTANCE.getConfigContent(PlugEffectWhiteBlackConstants.DYNAMIC_CONFIG_STRATEGY);
        String value = PlugEffectStategyCache.INSTANCE.getConfigContent(PlugEffectWhiteBlackConstants.DYNAMIC_CONFIG__VALUE);
        //全部生效
        if (StringUtils.equalsIgnoreCase(PlugEffectWhiteBlackConstants.STRATEGY_ALL, strategy)) {
            return true;
        }
        //全部不生效
        if (StringUtils.equalsIgnoreCase(PlugEffectWhiteBlackConstants.STRATEGY_NONE, strategy)) {
            return false;
        }
        //白名单-插件生效
        if (StringUtils.equalsIgnoreCase(PlugEffectWhiteBlackConstants.STRATEGY_WHITE, strategy)) {
            if (value.contains(serviceName)) {
                return true;
            } else {
                return false;
            }
        }
        //黑名单-插件不生效
        if (StringUtils.equalsIgnoreCase(PlugEffectWhiteBlackConstants.STRATEGY_BLACK, strategy)) {
            if (value.contains(serviceName)) {
                return false;
            } else {
                return true;
            }
        }
        return false;
    }

    /**
     * 判断url是否包含指定域名
     * @param url
     * @return
     */
    public static boolean isUrlContainsRealmName(String url) {
        String realmName = config.getRealmName();
        if (StringUtils.isBlank(realmName)) {
            return false;
        }
        if (realmName.contains(",")) {
            String[] names = realmName.split(",");
            for (String name : names) {
                if (url.contains(name)) {
                    return true;
                }
            }
            return false;
        }
        return url.contains(realmName);
    }

    /**
     * 判断主机名称是否为设置的域名
     * @param host
     * @return
     */
    public static boolean isHostEqualRealmName(String host) {
        String realmName = config.getRealmName();
        if (StringUtils.isBlank(realmName)) {
            return false;
        }
        if (realmName.contains(",")) {
            String[] names = realmName.split(",");
            for (String name : names) {
                if (StringUtils.equalsIgnoreCase(host, name)) {
                    return true;
                }
            }
            return false;
        }
        return StringUtils.equalsIgnoreCase(host, realmName);
    }

    /**
     * 判断是否允许请求通过插件
     * @param realmStr
     * @param serviceName
     * @param isByEqual
     * @return
     */
    public static boolean isNotAllowRun(String realmStr, String serviceName, boolean isByEqual) {
        if (isByEqual) {
            if (!PlugEffectWhiteBlackUtils.isHostEqualRealmName(realmStr)) {
                return true;
            }
        } else {
            if (!PlugEffectWhiteBlackUtils.isUrlContainsRealmName(realmStr)) {
                return true;
            }
        }
        return !PlugEffectWhiteBlackUtils.isPlugEffect(serviceName);
    }
}
