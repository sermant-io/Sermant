/*
 * Copyright (C) 2021-2024 Sermant Authors. All rights reserved.
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

package com.huaweicloud.sermant.core.plugin.agent;

import com.huaweicloud.sermant.core.config.ConfigManager;
import com.huaweicloud.sermant.core.plugin.Plugin;
import com.huaweicloud.sermant.core.plugin.agent.collector.PluginCollector;
import com.huaweicloud.sermant.core.plugin.agent.declarer.PluginDescription;
import com.huaweicloud.sermant.core.plugin.agent.enhance.ClassLoaderDeclarer;
import com.huaweicloud.sermant.core.service.ServiceConfig;

import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.agent.builder.AgentBuilder.RedefinitionStrategy;
import net.bytebuddy.agent.builder.AgentBuilder.RedefinitionStrategy.BatchAllocator.ForTotal;
import net.bytebuddy.agent.builder.AgentBuilder.RedefinitionStrategy.DiscoveryStrategy.Reiterating;
import net.bytebuddy.agent.builder.ResettableClassFileTransformer;

import java.lang.instrument.Instrumentation;
import java.util.List;

/**
 * Bytecode enhancement manager
 *
 * @author HapThorin
 * @version 1.0.0
 * @since 2022-01-22
 */
public class ByteEnhanceManager {
    private static Instrumentation instrumentationCache;

    private static BufferedAgentBuilder builder;

    private ByteEnhanceManager() {
    }

    /**
     * Initialization
     *
     * @param instrumentation instrumentation
     */
    public static void init(Instrumentation instrumentation) {
        instrumentationCache = instrumentation;
        builder = BufferedAgentBuilder.build();

        // Once initialization is complete, an Action is added to add bytecode enhancements introduced directly by
        // the framework
        enhanceForFramework();
    }

    /**
     * Install classloader enhanced bytecode for premain only
     */
    public static void enhance() {
        builder.install(instrumentationCache);
    }

    /**
     * Bytecode enhancement based on plugins that support static installation
     *
     * @param plugin plugin that supports static installation
     */
    public static void enhanceStaticPlugin(Plugin plugin) {
        if (plugin.isDynamic()) {
            return;
        }
        builder.addPlugins(PluginCollector.getDescriptions(plugin));
    }

    /**
     * Bytecode enhancement based on plugins that support dynamic installation
     *
     * @param plugin plugin that supports dynamic installation
     */
    public static void enhanceDynamicPlugin(Plugin plugin) {
        if (!plugin.isDynamic()) {
            return;
        }
        List<PluginDescription> plugins = PluginCollector.getDescriptions(plugin);
        ResettableClassFileTransformer resettableClassFileTransformer = BufferedAgentBuilder.build()
                .addPlugins(plugins).install(instrumentationCache);
        plugin.setClassFileTransformer(resettableClassFileTransformer);
    }

    /**
     * Uninstall bytecode enhancements for plugins that support dynamic installation
     *
     * @param plugin plugin that supports dynamic installation
     */
    public static void unEnhanceDynamicPlugin(Plugin plugin) {
        if (!plugin.isDynamic()) {
            return;
        }
        plugin.getClassFileTransformer().reset(instrumentationCache, RedefinitionStrategy.RETRANSFORMATION,
                Reiterating.INSTANCE, ForTotal.INSTANCE,
                AgentBuilder.RedefinitionStrategy.Listener.StreamWriting.toSystemOut());
    }

    private static void enhanceForFramework() {
        enhanceForInjectService();
    }

    /**
     * An enhancement to the classloader was introduced to help inject classes work with Sermant classes
     */
    private static void enhanceForInjectService() {
        if (ConfigManager.getConfig(ServiceConfig.class).isInjectEnable()) {
            builder.addEnhance(new ClassLoaderDeclarer());
        }
    }
}
