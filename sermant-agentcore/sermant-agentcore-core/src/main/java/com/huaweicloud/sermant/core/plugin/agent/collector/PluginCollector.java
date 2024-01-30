/*
 * Copyright (C) 2021-2022 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huaweicloud.sermant.core.plugin.agent.collector;

import com.huaweicloud.sermant.core.common.LoggerFactory;
import com.huaweicloud.sermant.core.plugin.Plugin;
import com.huaweicloud.sermant.core.plugin.agent.declarer.AbstractPluginDescription;
import com.huaweicloud.sermant.core.plugin.agent.declarer.InterceptDeclarer;
import com.huaweicloud.sermant.core.plugin.agent.declarer.PluginDeclarer;
import com.huaweicloud.sermant.core.plugin.agent.declarer.PluginDescription;
import com.huaweicloud.sermant.core.plugin.agent.matcher.ClassMatcher;
import com.huaweicloud.sermant.core.plugin.agent.matcher.ClassTypeMatcher;
import com.huaweicloud.sermant.core.plugin.agent.transformer.ReentrantTransformer;
import com.huaweicloud.sermant.core.plugin.classloader.PluginClassLoader;

import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType.Builder;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.utility.JavaModule;

import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 插件收集器，用于从所有插件中获取插件描述器
 *
 * @author HapThorin
 * @version 1.0.0
 * @since 2022-01-26
 */
public class PluginCollector {
    private static final Logger LOGGER = LoggerFactory.getLogger();

    private PluginCollector() {
    }

    /**
     * 从插件收集器中解析出所有的插件描述器，对插件声明器采用配置的合并策略
     *
     * @param plugin 需要解析插件描述的插件
     * @return 插件描述器列表
     */
    public static List<PluginDescription> getDescriptions(Plugin plugin) {
        final List<PluginDescription> descriptions = new ArrayList<>();

        // 通过加载插件Declarer并合并创建插件描述器
        descriptions.add(combinePluginDeclarers(plugin));

        // 直接加载插件描述器
        descriptions.addAll(getDescriptions(plugin.getPluginClassLoader()));
        return descriptions;
    }

    /**
     * 从插件收集器中获取所有插件描述器
     *
     * @param classLoader 类加载器
     * @return 插件描述器集
     */
    private static List<? extends PluginDescription> getDescriptions(ClassLoader classLoader) {
        final List<PluginDescription> descriptions = new ArrayList<>();
        for (PluginDescription description : loadDescriptions(classLoader)) {
            descriptions.add(description);
        }
        return descriptions;
    }

    /**
     * 合并插件中的插件声明器为一个插件描述器
     *
     * @param plugin 需要合并的插件
     * @return 插件描述器
     */
    private static PluginDescription combinePluginDeclarers(Plugin plugin) {
        final Map<String, List<PluginDeclarer>> nameCombinedMap = new HashMap<>();
        final List<PluginDeclarer> combinedList = new ArrayList<>();
        List<? extends PluginDeclarer> declarers = getDeclarers(plugin.getPluginClassLoader());
        for (PluginDeclarer pluginDeclarer : declarers) {
            final ClassMatcher classMatcher = pluginDeclarer.getClassMatcher();
            if (classMatcher instanceof ClassTypeMatcher) {
                for (String typeName : ((ClassTypeMatcher) classMatcher).getTypeNames()) {
                    List<PluginDeclarer> nameCombinedList = nameCombinedMap.computeIfAbsent(typeName,
                            key -> new ArrayList<>());
                    nameCombinedList.add(pluginDeclarer);
                }
            } else {
                combinedList.add(pluginDeclarer);
            }
        }
        return createPluginDescription(plugin, nameCombinedMap, combinedList);
    }

    private static AbstractPluginDescription createPluginDescription(Plugin plugin,
            Map<String, List<PluginDeclarer>> nameCombinedMap, List<PluginDeclarer> combinedList) {
        return new AbstractPluginDescription() {
            @Override
            public Builder<?> transform(Builder<?> builder, TypeDescription typeDescription, ClassLoader classLoader,
                    JavaModule javaModule, ProtectionDomain protectionDomain) {
                final List<PluginDeclarer> pluginDeclarers = nameCombinedMap.get(typeDescription.getActualName());
                final List<InterceptDeclarer> interceptDeclarers = new ArrayList<>();
                for (PluginDeclarer pluginDeclarer : pluginDeclarers) {
                    ClassLoader loader = pluginDeclarer.getClass().getClassLoader();
                    if (loader instanceof PluginClassLoader) {
                        PluginClassLoader pluginClassLoader = (PluginClassLoader) loader;
                        pluginClassLoader.setLocalLoader(classLoader);
                        interceptDeclarers.addAll(Arrays.asList(
                                pluginDeclarer.getInterceptDeclarers(ClassLoader.getSystemClassLoader())));
                        pluginClassLoader.removeLocalLoader();
                    } else {
                        interceptDeclarers.addAll(Arrays.asList(
                                pluginDeclarer.getInterceptDeclarers(ClassLoader.getSystemClassLoader())));
                    }
                }
                return new ReentrantTransformer(interceptDeclarers.toArray(new InterceptDeclarer[0]), plugin).transform(
                        builder, typeDescription, classLoader, javaModule, protectionDomain);
            }

            @Override
            public boolean matches(TypeDescription target) {
                final String typeName = target.getActualName();
                for (PluginDeclarer declarer : combinedList) {
                    if (matchTarget(declarer.getClassMatcher(), target)) {
                        List<PluginDeclarer> declarers = nameCombinedMap.computeIfAbsent(typeName,
                                key -> new ArrayList<>());
                        if (!declarers.contains(declarer)) {
                            declarers.add(declarer);
                        }
                    }
                }
                return nameCombinedMap.containsKey(typeName);
            }
        };
    }

    /**
     * 从插件收集器中获取所有插件声明器
     *
     * @param classLoader 类加载器
     * @return 插件声明器集
     */
    private static List<? extends PluginDeclarer> getDeclarers(ClassLoader classLoader) {
        final List<PluginDeclarer> declares = new ArrayList<>();
        for (PluginDeclarer declarer : loadDeclarers(classLoader)) {
            if (declarer.isEnabled()) {
                declares.add(declarer);
            }
        }
        return declares;
    }

    private static Iterable<? extends PluginDeclarer> loadDeclarers(ClassLoader classLoader) {
        return ServiceLoader.load(PluginDeclarer.class, classLoader);
    }

    private static Iterable<? extends PluginDescription> loadDescriptions(ClassLoader classLoader) {
        return ServiceLoader.load(PluginDescription.class, classLoader);
    }

    private static boolean matchTarget(ElementMatcher<TypeDescription> matcher, TypeDescription target) {
        try {
            return matcher.matches(target);
        } catch (Exception exception) {
            LOGGER.log(Level.WARNING, "Exception occurs when math target: " + target.getActualName() + ",{0}",
                    exception.getMessage());
            return false;
        }
    }
}
