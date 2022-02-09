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

package com.huawei.sermant.core.plugin.agent.collector;

import com.huawei.sermant.core.config.ConfigManager;
import com.huawei.sermant.core.plugin.agent.config.AgentConfig;
import com.huawei.sermant.core.plugin.agent.declarer.AbstractPluginDescription;
import com.huawei.sermant.core.plugin.agent.declarer.InterceptDeclarer;
import com.huawei.sermant.core.plugin.agent.declarer.PluginDeclarer;
import com.huawei.sermant.core.plugin.agent.declarer.PluginDescription;
import com.huawei.sermant.core.plugin.agent.declarer.SuperTypeDeclarer;
import com.huawei.sermant.core.plugin.agent.matcher.ClassMatcher;
import com.huawei.sermant.core.plugin.agent.matcher.ClassTypeMatcher;
import com.huawei.sermant.core.plugin.agent.transformer.AdviceTransformer;
import com.huawei.sermant.core.plugin.agent.transformer.BootstrapTransformer;

import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.utility.JavaModule;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ServiceLoader;

/**
 * 插件收集器管理器，用于从所有插件收集器中获取插件描述器
 *
 * @author HapThorin
 * @version 1.0.0
 * @since 2022-01-26
 */
public class PluginCollectorManager {
    /**
     * 通过spi检索所有配置的插件收集器
     */
    private static final Iterable<PluginCollector> COLLECTORS = ServiceLoader.load(PluginCollector.class);

    private PluginCollectorManager() {
    }

    /**
     * 从插件收集器中解析出所有的插件描述器，对插件声明器采用配置的合并策略
     *
     * @return 插件描述器列表
     */
    public static List<PluginDescription> getPlugins() {
        return getPlugins(ConfigManager.getConfig(AgentConfig.class).getCombineStrategy());
    }

    /**
     * 从插件收集器中解析出所有的插件描述器
     *
     * @param strategy 插件声明器的合并策略
     * @return 插件描述器列表
     */
    public static List<PluginDescription> getPlugins(AgentConfig.CombineStrategy strategy) {
        final List<PluginDescription> plugins = new ArrayList<>();
        plugins.addAll(combinePlugins(getDeclarers(), strategy));
        plugins.addAll(getDescriptions());
        return plugins;
    }

    /**
     * 从插件收集器中获取所有插件声明器
     *
     * @return 插件声明器集
     */
    private static List<? extends PluginDeclarer> getDeclarers() {
        final List<PluginDeclarer> declares = new ArrayList<>();
        for (PluginCollector collector : COLLECTORS) {
            for (PluginDeclarer declarer : collector.getDeclarers()) {
                declares.add(declarer);
            }
        }
        return declares;
    }

    /**
     * 从插件收集器中获取所有插件描述器
     *
     * @return 插件描述器集
     */
    private static List<? extends PluginDescription> getDescriptions() {
        final List<PluginDescription> descriptions = new ArrayList<>();
        for (PluginCollector collector : COLLECTORS) {
            for (PluginDescription description : collector.getDescriptions()) {
                descriptions.add(description);
            }
        }
        return descriptions;
    }

    /**
     * 合并所有的插件声明器
     *
     * @param declarers 插件声明器列表
     * @param strategy  插件声明器合并策略
     * @return 合并所得的插件描述器列表
     */
    private static List<PluginDescription> combinePlugins(List<? extends PluginDeclarer> declarers,
            AgentConfig.CombineStrategy strategy) {
        final List<PluginDescription> plugins = new ArrayList<>();
        if (!declarers.isEmpty()) {
            switch (strategy) {
                case NONE:
                    plugins.addAll(describeDeclarers(declarers));
                    break;
                case BY_NAME:
                    plugins.addAll(combineDeclarersByName(declarers));
                    break;
                case ALL:
                    plugins.add(combineAllDeclarers(declarers));
                    break;
                default:
                    throw new IllegalArgumentException(String.format(Locale.ROOT,
                            "Unknown combine strategy %s. ", strategy));
            }
        }
        return plugins;
    }

    /**
     * 直接将所有插件声明器描述为插件描述器
     *
     * @param declarers 插件声明器集
     * @return 插件描述器
     */
    private static List<PluginDescription> describeDeclarers(Iterable<? extends PluginDeclarer> declarers) {
        final List<PluginDescription> plugins = new ArrayList<>();
        for (PluginDeclarer pluginDeclarer : declarers) {
            plugins.add(describeDeclarer(pluginDeclarer));
        }
        return plugins;
    }

    /**
     * 将一个插件声明器描述为插件描述器
     *
     * @param declarer 插件声明器
     * @return 插件描述器
     */
    private static PluginDescription describeDeclarer(PluginDeclarer declarer) {
        return new AbstractPluginDescription() {
            @Override
            public boolean matches(TypeDescription target) {
                return declarer.getClassMatcher().matches(target);
            }

            @Override
            public DynamicType.Builder<?> transform(DynamicType.Builder<?> builder, TypeDescription typeDescription,
                    ClassLoader classLoader, JavaModule module) {
                if (classLoader == null) {
                    return new BootstrapTransformer(
                            declarer.getInterceptDeclarers(ClassLoader.getSystemClassLoader())
                    ).transform(builder, typeDescription, null, module);
                } else {
                    return new AdviceTransformer(
                            declarer.getInterceptDeclarers(classLoader), declarer.getSuperTypeDeclarers()
                    ).transform(builder, typeDescription, classLoader, module);
                }
            }
        };
    }

    /**
     * 仅通过名称合并插件声明器为一个插件描述器，其他的直接描述{@link #describeDeclarer}
     *
     * @param declarers 插件声明器集
     * @return 插件描述器集
     */
    private static List<PluginDescription> combineDeclarersByName(Iterable<? extends PluginDeclarer> declarers) {
        final List<PluginDescription> plugins = new ArrayList<>();
        final Map<String, List<PluginDeclarer>> nameCombinedMap = new HashMap<>();
        for (PluginDeclarer pluginDeclarer : declarers) {
            final ClassMatcher classMatcher = pluginDeclarer.getClassMatcher();
            if (classMatcher instanceof ClassTypeMatcher) {
                for (String typeName : ((ClassTypeMatcher) classMatcher).getTypeNames()) {
                    List<PluginDeclarer> nameCombinedList = nameCombinedMap.get(typeName);
                    if (nameCombinedList == null) {
                        nameCombinedList = new ArrayList<>();
                        nameCombinedMap.put(typeName, nameCombinedList);
                    }
                    nameCombinedList.add(pluginDeclarer);
                }
            } else {
                plugins.add(describeDeclarer(pluginDeclarer));
            }
        }
        if (!nameCombinedMap.isEmpty()) {
            plugins.add(createNameCombinedDescription(nameCombinedMap));
        }
        return plugins;
    }

    /**
     * 创建根据名称合并插件声明器的插件描述器
     *
     * @param nameCombinedMap 插件声明器及其声明的被增强类名集
     * @return 插件描述器
     */
    private static PluginDescription createNameCombinedDescription(Map<String, List<PluginDeclarer>> nameCombinedMap) {
        return new AbstractPluginDescription() {
            @Override
            public boolean matches(TypeDescription target) {
                return nameCombinedMap.containsKey(target.getActualName());
            }

            @Override
            public DynamicType.Builder<?> transform(DynamicType.Builder<?> builder, TypeDescription typeDescription,
                    ClassLoader classLoader, JavaModule module) {
                return nameCombinedTransform(builder, typeDescription, classLoader, module, nameCombinedMap);
            }
        };
    }

    /**
     * 合并所有插件声明器为一个插件描述器
     *
     * @param declarers 插件声明器集
     * @return 插件描述器
     */
    private static PluginDescription combineAllDeclarers(Iterable<? extends PluginDeclarer> declarers) {
        final Map<String, List<PluginDeclarer>> nameCombinedMap = new HashMap<>();
        final List<PluginDeclarer> combinedList = new ArrayList<>();
        for (PluginDeclarer pluginDeclarer : declarers) {
            final ClassMatcher classMatcher = pluginDeclarer.getClassMatcher();
            if (classMatcher instanceof ClassTypeMatcher) {
                for (String typeName : ((ClassTypeMatcher) classMatcher).getTypeNames()) {
                    List<PluginDeclarer> nameCombinedList = nameCombinedMap.get(typeName);
                    if (nameCombinedList == null) {
                        nameCombinedList = new ArrayList<>();
                        nameCombinedMap.put(typeName, nameCombinedList);
                    }
                    nameCombinedList.add(pluginDeclarer);
                }
            } else {
                combinedList.add(pluginDeclarer);
            }
        }
        return createAllCombinedDescription(nameCombinedMap, combinedList);
    }

    /**
     * 创建合并全部插件声明器的插件描述器
     *
     * @param nameCombinedMap 插件声明器及其声明的被增强类名集
     * @param combinedList    其他模糊匹配的插件声明器列表
     * @return 插件描述器
     */
    private static PluginDescription createAllCombinedDescription(Map<String, List<PluginDeclarer>> nameCombinedMap,
            List<PluginDeclarer> combinedList) {
        return new AbstractPluginDescription() {
            @Override
            public DynamicType.Builder<?> transform(DynamicType.Builder<?> builder, TypeDescription typeDescription,
                    ClassLoader classLoader, JavaModule module) {
                return nameCombinedTransform(builder, typeDescription, classLoader, module, nameCombinedMap);
            }

            @Override
            public boolean matches(TypeDescription target) {
                final String typeName = target.getActualName();
                for (PluginDeclarer pluginDeclarer : combinedList) {
                    if (pluginDeclarer.getClassMatcher().matches(target)) {
                        List<PluginDeclarer> declarers = nameCombinedMap.get(typeName);
                        if (declarers == null) {
                            declarers = new ArrayList<>();
                            nameCombinedMap.put(typeName, declarers);
                        }
                        declarers.add(pluginDeclarer);
                    }
                }
                return nameCombinedMap.containsKey(typeName);
            }
        };
    }

    /**
     * 处理按名称合并的插件声明器的{@link net.bytebuddy.agent.builder.AgentBuilder.Transformer#transform}方法
     *
     * @param builder         byte-buddy的动态构建器
     * @param typeDescription 被增强类的描述器
     * @param classLoader     被增强类的类加载器
     * @param module          byte-buddy的java模块对象
     * @param nameCombinedMap 插件声明器及其声明的被增强类名集
     * @return 构建器
     */
    private static DynamicType.Builder<?> nameCombinedTransform(DynamicType.Builder<?> builder,
            TypeDescription typeDescription, ClassLoader classLoader, JavaModule module,
            Map<String, List<PluginDeclarer>> nameCombinedMap) {
        final List<PluginDeclarer> pluginDeclarers = nameCombinedMap.remove(typeDescription.getActualName());
        final List<InterceptDeclarer> interceptDeclarers = new ArrayList<>();
        if (classLoader == null) {
            for (PluginDeclarer pluginDeclarer : pluginDeclarers) {
                interceptDeclarers.addAll(
                        Arrays.asList(pluginDeclarer.getInterceptDeclarers(ClassLoader.getSystemClassLoader())));
            }
            return new BootstrapTransformer(
                    interceptDeclarers.toArray(new InterceptDeclarer[0])
            ).transform(builder, typeDescription, null, module);
        } else {
            final List<SuperTypeDeclarer> superTypeDeclarers = new ArrayList<>();
            for (PluginDeclarer pluginDeclarer : pluginDeclarers) {
                interceptDeclarers.addAll(Arrays.asList(pluginDeclarer.getInterceptDeclarers(classLoader)));
                superTypeDeclarers.addAll(Arrays.asList(pluginDeclarer.getSuperTypeDeclarers()));
            }
            return new AdviceTransformer(
                    interceptDeclarers.toArray(new InterceptDeclarer[0]),
                    superTypeDeclarers.toArray(new SuperTypeDeclarer[0])
            ).transform(builder, typeDescription, classLoader, module);
        }
    }
}
