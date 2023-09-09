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
 * 字节码增强管理器
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
     * 初始化
     *
     * @param instrumentation instrumentation
     */
    public static void init(Instrumentation instrumentation) {
        instrumentationCache = instrumentation;
        builder = BufferedAgentBuilder.build();

        // 初始化完成后，新增Action用于添加框架直接引入的字节码增强
        enhanceForFramework();
    }

    /**
     * 安装类加载器增强字节码，仅用于premain方式启动
     */
    public static void enhance() {
        builder.install(instrumentationCache);
    }

    /**
     * 基于支持静态安装的插件进行字节码增强
     *
     * @param plugin 支持静态安装的插件
     */
    public static void enhanceStaticPlugin(Plugin plugin) {
        if (plugin.isDynamic()) {
            return;
        }
        builder.addPlugins(PluginCollector.getDescriptions(plugin));
    }

    /**
     * 基于支持动态安装的插件进行字节码增强
     *
     * @param plugin 支持动态安装的插件
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
     * 卸载支持动态安装的插件的字节码增强
     *
     * @param plugin 支持动态安装的插件
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
     * 引入对类加载器的增强，帮助inject的类可以使用到Sermant的类
     */
    private static void enhanceForInjectService() {
        if (ConfigManager.getConfig(ServiceConfig.class).isInjectEnable()) {
            builder.addEnhance(new ClassLoaderDeclarer());
        }
    }
}
