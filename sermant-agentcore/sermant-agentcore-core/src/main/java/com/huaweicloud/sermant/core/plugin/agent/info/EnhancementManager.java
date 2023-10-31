/*
 * Copyright (C) 2023-2023 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huaweicloud.sermant.core.plugin.agent.info;

import com.huaweicloud.sermant.core.plugin.Plugin;
import com.huaweicloud.sermant.core.plugin.agent.interceptor.Interceptor;
import com.huaweicloud.sermant.core.utils.StringUtils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 存储增强信息的静态类
 *
 * @author tangle
 * @since 2023-11-02
 */
public class EnhancementManager {
    private static final Map<String, Map<String, Set<String>>> ENHANCEMENTS = new HashMap<>();

    private EnhancementManager() {
    }

    public static Map<String, Map<String, Set<String>>> getEnhancements() {
        return ENHANCEMENTS;
    }

    /**
     * 添加拦截器信息
     *
     * @param plugin 插件
     * @param interceptorList 拦截器列表
     * @param classLoader 类加载器
     * @param methodDesc 被增强方法的信息
     */
    public static void addEnhancements(Plugin plugin, List<Interceptor> interceptorList, ClassLoader classLoader,
            String methodDesc) {
        String enhancementKey = combinePluginInfo(plugin);
        if (!StringUtils.isEmpty(methodDesc)) {
            Map<String, Set<String>> methodDescMap = ENHANCEMENTS.computeIfAbsent(enhancementKey,
                    key -> new HashMap<>());
            String methodDescKey = combineEnhanceInfo(methodDesc, classLoader);
            Set<String> interceptorSet = methodDescMap.computeIfAbsent(methodDescKey, key -> new HashSet<>());
            for (Interceptor interceptor : interceptorList) {
                interceptorSet.add(interceptor.getClass().getCanonicalName());
            }
        }
    }

    /**
     * 卸载插件时清除该插件的增强信息
     *
     * @param plugin 插件
     */
    public static void removePluginEnhancements(Plugin plugin) {
        ENHANCEMENTS.remove(combinePluginInfo(plugin));
    }

    /**
     * 清理缓存的增强信息
     */
    public static void shutdown() {
        ENHANCEMENTS.clear();
    }

    /**
     * 拼接插件信息
     */
    private static String combinePluginInfo(Plugin plugin) {
        return plugin.getName() + ":" + plugin.getVersion();
    }

    /**
     * 拼接增强信息
     */
    private static String combineEnhanceInfo(String methodDesc, ClassLoader classLoader) {
        return methodDesc + "@" + classLoader;
    }
}
