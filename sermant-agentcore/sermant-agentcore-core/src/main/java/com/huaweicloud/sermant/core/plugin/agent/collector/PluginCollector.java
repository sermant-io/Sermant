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
 * Plugin collector, which gets plugin descriptors from all plugins
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
     * Resolve all plugin descriptions from the plugin collector and apply the configured merge policy to the
     * plugin declarers
     *
     * @param plugin plugin
     * @return list of PluginDescription
     */
    public static List<PluginDescription> getDescriptions(Plugin plugin) {
        final List<PluginDescription> descriptions = new ArrayList<>();

        // Create a plugin description by loading the plugin declarer and merging
        descriptions.add(combinePluginDeclarers(plugin));

        // Load the plugin description directly
        descriptions.addAll(getDescriptions(plugin.getPluginClassLoader()));
        return descriptions;
    }

    /**
     * Gets all plugin descriptions from the plugin collector
     *
     * @param classLoader classLoader
     * @return list of PluginDescription
     */
    private static List<? extends PluginDescription> getDescriptions(ClassLoader classLoader) {
        final List<PluginDescription> descriptions = new ArrayList<>();
        for (PluginDescription description : loadDescriptions(classLoader)) {
            descriptions.add(description);
        }
        return descriptions;
    }

    /**
     * Merge the plugin declarers in a plugin into a plugin description
     *
     * @param plugin plugin
     * @return PluginDescription
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
                return doTransform(builder, typeDescription, classLoader, javaModule, protectionDomain, nameCombinedMap,
                        plugin);
            }

            @Override
            public boolean matches(TypeDescription target) {
                final String typeName = target.getActualName();
                doMatch(target, typeName, combinedList, nameCombinedMap);
                return nameCombinedMap.containsKey(typeName);
            }
        };
    }

    private static void doMatch(TypeDescription target, String typeName, List<PluginDeclarer> combinedList,
            Map<String, List<PluginDeclarer>> nameCombinedMap) {
        for (PluginDeclarer declarer : combinedList) {
            if (matchTarget(declarer.getClassMatcher(), target)) {
                List<PluginDeclarer> declarers = nameCombinedMap.computeIfAbsent(typeName,
                        key -> new ArrayList<>());
                if (!declarers.contains(declarer)) {
                    declarers.add(declarer);
                }
            }
        }
    }

    private static Builder<?> doTransform(Builder<?> builder, TypeDescription typeDescription, ClassLoader classLoader,
            JavaModule javaModule, ProtectionDomain protectionDomain, Map<String, List<PluginDeclarer>> nameCombinedMap,
            Plugin plugin) {
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

    /**
     * Gets all plugin declarers from the plugin collectors
     *
     * @param classLoader classLoader
     * @return list of PluginDeclarer
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
