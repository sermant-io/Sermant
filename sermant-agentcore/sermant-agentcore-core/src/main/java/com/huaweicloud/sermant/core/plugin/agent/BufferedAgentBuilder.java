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

import com.huaweicloud.sermant.core.classloader.FrameworkClassLoader;
import com.huaweicloud.sermant.core.common.CommonConstant;
import com.huaweicloud.sermant.core.common.LoggerFactory;
import com.huaweicloud.sermant.core.config.ConfigManager;
import com.huaweicloud.sermant.core.event.collector.FrameworkEventCollector;
import com.huaweicloud.sermant.core.plugin.Plugin;
import com.huaweicloud.sermant.core.plugin.agent.config.AgentConfig;
import com.huaweicloud.sermant.core.plugin.agent.declarer.AbstractPluginDeclarer;
import com.huaweicloud.sermant.core.plugin.agent.declarer.AbstractPluginDescription;
import com.huaweicloud.sermant.core.plugin.agent.declarer.PluginDescription;
import com.huaweicloud.sermant.core.plugin.agent.transformer.ReentrantTransformer;
import com.huaweicloud.sermant.core.plugin.classloader.PluginClassLoader;
import com.huaweicloud.sermant.core.plugin.classloader.ServiceClassLoader;
import com.huaweicloud.sermant.core.utils.FileUtils;

import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.agent.builder.AgentBuilder.Default;
import net.bytebuddy.agent.builder.ResettableClassFileTransformer;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.description.type.TypeList.Generic;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.dynamic.DynamicType.Builder;
import net.bytebuddy.utility.JavaModule;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.instrument.Instrumentation;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.ProtectionDomain;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * {@link AgentBuilder}的包装类提供一系列按配置进行的默认操作，并提供插件的增强操作
 *
 * @author HapThorin
 * @version 1.0.0
 * @since 2022-01-22
 */
public class BufferedAgentBuilder {
    /**
     * 日志
     */
    private static final Logger LOGGER = LoggerFactory.getLogger();

    /**
     * 增强配置
     */
    private final AgentConfig config = ConfigManager.getConfig(AgentConfig.class);

    /**
     * 构建行为集
     */
    private final List<BuilderAction> actions = new ArrayList<>();

    /**
     * 为对框架类的增强维护一个虚拟的插件，用于记录adviceKey锁和已经创建的拦截器
     */
    private final Plugin virtualPlugin = new Plugin("virtual-plugin", null, false, null);

    private BufferedAgentBuilder() {
    }

    /**
     * 创建{@link BufferedAgentBuilder}并依据配置设置基础操作：
     * <pre>
     *     1.设置启动类加载器相关的增强策略，见{@link #setBootStrapStrategy}
     *     2.设置增强扫描过滤规则，见{@link #setIgnoredRule}
     *     3.设置增强时的扫描日志监听器，见{@link #setLogListener}
     *     4.设置输出增强后字节码的监听器，见{@link #setOutputListener}
     * </pre>
     *
     * @return BufferedAgentBuilder实例
     */
    public static BufferedAgentBuilder build() {
        return new BufferedAgentBuilder().setBootStrapStrategy()
                .setIgnoredRule()
                .setLogListener()
                .setOutputListener();
    }

    /**
     * 设置字节码增强的重定义策略，由{@link AgentConfig#isReTransformEnable()}而定
     * <pre>
     *     1.若不增强启动类加载器加载的类，则直接使用默认规则{@link AgentBuilder.RedefinitionStrategy#DISABLED}
     *     1.若增强启动类加载器加载的类，则使用规则{@link AgentBuilder.RedefinitionStrategy#RETRANSFORMATION}
     * </pre>
     *
     * @return BufferedAgentBuilder本身
     */
    private BufferedAgentBuilder setBootStrapStrategy() {
        if (!config.isReTransformEnable()) {
            return this;
        }
        return addAction(builder -> builder.with(AgentBuilder.RedefinitionStrategy.RETRANSFORMATION));
    }

    /**
     * 设置扫描的过滤规则
     * <p>注意，数组类型，8种基础类型，以及{@link ServiceClassLoader},{@link FrameworkClassLoader}加载的类默认不增强，直接被过滤
     * <p>其他类若符合配置中{@link AgentConfig#getIgnoredPrefixes}指定的前缀之一，则被过滤
     *
     * @return BufferedAgentBuilder本身
     */
    private BufferedAgentBuilder setIgnoredRule() {
        return addAction(builder -> builder.ignore(new IgnoredMatcher(config)));
    }

    /**
     * 设置输出日志的监听器，由{@link AgentConfig#isShowEnhanceLog()}而定
     * <p>使用{@link AgentBuilder.Listener.StreamWriting}转化为字符串信息后输出为日志
     * <p>注意，输出时使用的缓冲区将不会被释放，需要关注{@link AgentBuilder.Listener.StreamWriting}中单行信息的长度
     *
     * @return BufferedAgentBuilder本身
     */
    private BufferedAgentBuilder setLogListener() {
        if (!config.isShowEnhanceLog()) {
            return this;
        }
        return addAction(builder -> builder
                .with(new AgentBuilder.Listener.StreamWriting(new PrintStream(new ByteArrayOutputStream() {
                    private final byte[] separatorBytes =
                            System.lineSeparator().getBytes(CommonConstant.DEFAULT_CHARSET);

                    private final int separatorLength = separatorBytes.length;

                    @Override
                    public void flush() {
                        if (count < separatorLength) {
                            return;
                        }
                        for (int i = separatorLength - 1; i >= 0; i--) {
                            if (buf[count + i - separatorLength] != separatorBytes[i]) {
                                return;
                            }
                        }
                        String enhanceLog = new String(Arrays.copyOf(buf, count - separatorLength));
                        logAndCollectEvent(enhanceLog);
                        reset();
                    }

                    // 针对Byte-buddy中触发的Error及Warn级别日志上报事件
                    private void logAndCollectEvent(String enhanceLog) {
                        if (enhanceLog.contains(CommonConstant.ERROR)) {
                            FrameworkEventCollector.getInstance().collectTransformFailureEvent(enhanceLog);
                            return;
                        }
                        if (enhanceLog.contains(CommonConstant.TRANSFORM)) {
                            FrameworkEventCollector.getInstance().collectTransformSuccessEvent(enhanceLog);
                        }
                        LOGGER.info(enhanceLog);
                    }
                }, true))));
    }

    /**
     * 设置输出增强后字节码的监听器
     *
     * @return BufferedAgentBuilder本身
     */
    private BufferedAgentBuilder setOutputListener() {
        if (!config.isOutputEnhancedClasses()) {
            return this;
        }

        String outputPath = config.getEnhancedClassesOutputPath();
        final Path outputDirectory;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");
        String currentTime = LocalDateTime.now().format(formatter);
        if (outputPath == null || outputPath.isEmpty()) {
            outputDirectory = Paths.get(FileUtils.getAgentPath())
                    .resolve(CommonConstant.ENHANCED_CLASS_OUTPUT_PARENT_DIR).resolve(currentTime);
        } else {
            outputDirectory =
                    Paths.get(outputPath).resolve(CommonConstant.ENHANCED_CLASS_OUTPUT_PARENT_DIR).resolve(currentTime);
        }
        final File file;
        try {
            file = Files.createDirectories(outputDirectory).toFile();
        } catch (IOException e) {
            LOGGER.warning("Create enhanced class output directory fail!");
            return this;
        }
        return addAction(builder -> builder.with(new AgentBuilder.Listener.Adapter() {
            @Override
            public void onTransformation(TypeDescription typeDescription, ClassLoader classLoader, JavaModule module,
                    boolean loaded, DynamicType dynamicType) {
                try {
                    dynamicType.saveIn(file);
                } catch (IOException e) {
                    LOGGER.log(Level.WARNING, "Save class {0} byte code failed.", typeDescription.getTypeName());
                }
            }
        }));
    }

    /**
     * 添加插件
     *
     * @param plugins 插件描述列表
     * @return BufferedAgentBuilder AgentBuilder的封装
     */
    public BufferedAgentBuilder addPlugins(Iterable<PluginDescription> plugins) {
        return addAction(new BuilderAction() {
            @Override
            public AgentBuilder process(AgentBuilder builder) {
                AgentBuilder newBuilder = builder;
                for (PluginDescription plugin : plugins) {
                    // 此处必须赋值给newBuilder，不可在原builder上重复操作，否则上次循环中的操作会不生效
                    newBuilder = newBuilder.type(plugin).transform(plugin);
                }
                return newBuilder;
            }
        });
    }

    /**
     * 基于{@link com.huaweicloud.sermant.core.plugin.agent.declarer.PluginDeclarer}添加字节码增强
     *
     * @param pluginDeclarer 插件声明器
     */
    public void addEnhance(AbstractPluginDeclarer pluginDeclarer) {
        addAction(builder -> {
            PluginDescription pluginDescription = new AbstractPluginDescription() {
                final AbstractPluginDeclarer abstractPluginDeclarer = pluginDeclarer;

                @Override
                public Builder<?> transform(Builder<?> builder, TypeDescription typeDescription,
                        ClassLoader classLoader,
                        JavaModule module, ProtectionDomain protectionDomain) {
                    return new ReentrantTransformer(abstractPluginDeclarer.getInterceptDeclarers(classLoader),
                            virtualPlugin)
                            .transform(builder, typeDescription, classLoader, module, protectionDomain);
                }

                @Override
                public boolean matches(TypeDescription target) {
                    return abstractPluginDeclarer.getClassMatcher().matches(target);
                }
            };
            return builder.type(pluginDescription).transform(pluginDescription);
        });
    }

    /**
     * 添加行动
     *
     * @param action 行动
     * @return BufferedAgentBuilder本身
     */
    public BufferedAgentBuilder addAction(BuilderAction action) {
        actions.add(action);
        return this;
    }

    /**
     * 构建{@link AgentBuilder}，执行所有{@link BuilderAction}并执行{@link AgentBuilder#installOn(Instrumentation)}
     *
     * @param instrumentation Instrumentation对象
     * @return 安装结果，可重置的转换器，若无类元信息改动，调用其reset方法即可重置
     */
    public ResettableClassFileTransformer install(Instrumentation instrumentation) {
        AgentBuilder builder = new Default().disableClassFormatChanges();
        for (BuilderAction action : actions) {
            builder = action.process(builder);
        }
        return builder.installOn(instrumentation);
    }

    /**
     * 忽略匹配器
     *
     * @author provenceee
     * @since 2022-11-17
     */
    private static class IgnoredMatcher implements AgentBuilder.RawMatcher {
        private final Set<String> ignoredPrefixes;

        private final Set<String> serviceInjectList;

        private final Set<String> ignoredInterfaces;

        IgnoredMatcher(AgentConfig config) {
            ignoredPrefixes = config.getIgnoredPrefixes();
            serviceInjectList = config.getServiceInjectList();
            ignoredInterfaces = config.getIgnoredInterfaces();
        }

        @Override
        public boolean matches(TypeDescription typeDesc, ClassLoader classLoader, JavaModule javaModule,
                Class<?> classBeingRedefined, ProtectionDomain protectionDomain) {
            return isArrayOrPrimitive(typeDesc) || checkClassLoader(typeDesc, classLoader)
                    || isIgnoredPrefixes(typeDesc) || isIgnoredInterfaces(typeDesc);
        }

        private boolean isArrayOrPrimitive(TypeDescription typeDesc) {
            return typeDesc.isArray() || typeDesc.isPrimitive();
        }

        private boolean checkClassLoader(TypeDescription typeDesc, ClassLoader classLoader) {
            if (classLoader instanceof FrameworkClassLoader) {
                return true;
            }
            if (classLoader instanceof PluginClassLoader) {
                return true;
            }
            if (classLoader instanceof ServiceClassLoader) {
                return !serviceInjectList.contains(typeDesc.getTypeName());
            }
            return false;
        }

        private boolean isIgnoredPrefixes(TypeDescription typeDesc) {
            if (ignoredPrefixes.isEmpty()) {
                return false;
            }
            for (String ignoredPrefix : ignoredPrefixes) {
                if (typeDesc.getTypeName().startsWith(ignoredPrefix)) {
                    return true;
                }
            }
            return false;
        }

        private boolean isIgnoredInterfaces(TypeDescription typeDesc) {
            if (ignoredInterfaces == null || ignoredInterfaces.isEmpty()) {
                return false;
            }
            Generic interfaces = typeDesc.getInterfaces();
            if (interfaces == null || interfaces.isEmpty()) {
                return false;
            }
            for (TypeDescription.Generic interfaceClass : interfaces) {
                if (ignoredInterfaces.contains(interfaceClass.getTypeName())) {
                    return true;
                }
            }
            return false;
        }
    }

    /**
     * 构建行为
     *
     * @since 2022-01-22
     */
    public interface BuilderAction {
        /**
         * 执行构建行为
         *
         * @param builder 构建器
         * @return 构建器
         */
        AgentBuilder process(AgentBuilder builder);
    }
}
