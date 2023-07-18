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
import com.huaweicloud.sermant.core.config.ConfigManager;
import com.huaweicloud.sermant.core.plugin.agent.config.AgentConfig;
import com.huaweicloud.sermant.core.plugin.agent.declarer.AbstractPluginDescription;
import com.huaweicloud.sermant.core.plugin.agent.declarer.PluginDeclarer;
import com.huaweicloud.sermant.core.plugin.agent.declarer.PluginDescription;
import com.huaweicloud.sermant.core.plugin.agent.transformer.DefaultTransformer;

import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.utility.JavaModule;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 插件收集器管理器，用于从所有插件收集器中获取插件描述器
 *
 * @author HapThorin
 * @version 1.0.0
 * @since 2022-01-26
 */
public class PluginCollectorManager {
    private static final Logger LOGGER = LoggerFactory.getLogger();

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
        plugins.addAll(combinePlugins(getDeclarers()));
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
                if (declarer.isEnabled()) {
                    declares.add(declarer);
                }
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
     * @return 合并所得的插件描述器列表
     */
    private static List<PluginDescription> combinePlugins(List<? extends PluginDeclarer> declarers) {
        final List<PluginDescription> plugins = new ArrayList<>();
        if (!declarers.isEmpty()) {
            plugins.addAll(describeDeclarers(declarers));
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
                return matchTarget(declarer.getClassMatcher(), target);
            }

            @Override
            public DynamicType.Builder<?> transform(DynamicType.Builder<?> builder, TypeDescription typeDescription,
                    ClassLoader classLoader, JavaModule module) {
                return new DefaultTransformer(declarer.getInterceptDeclarers(ClassLoader.getSystemClassLoader()))
                        .transform(builder, typeDescription, classLoader, module);
            }
        };
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
