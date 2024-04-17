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
import com.huaweicloud.sermant.god.common.SermantClassLoader;

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
 * The wrapper class for {@link AgentBuilder}, which provides a set of default actions as configured and provides
 * enhancement actions for plugins
 *
 * @author HapThorin
 * @version 1.0.0
 * @since 2022-01-22
 */
public class BufferedAgentBuilder {
    /**
     * logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger();

    /**
     * enhancement configuration
     */
    private final AgentConfig config = ConfigManager.getConfig(AgentConfig.class);

    /**
     * BuilderAction list
     */
    private final List<BuilderAction> actions = new ArrayList<>();

    /**
     * Maintain a virtual plugin for enhancements to the framework class to record adviceKey locks and the interceptors
     * that have been created
     */
    private final Plugin virtualPlugin = new Plugin("virtual-plugin", null, false, null);

    private BufferedAgentBuilder() {
    }

    /**
     * Create {@link BufferedAgentBuilder} and set the base actions according to the configuration:
     * <pre>
     *     1.Set the enhancement strategy associated with BootStrapClassLoader, see {@link #setBootStrapStrategy}
     *     2.Set enhancement scan filtering rules, see {@link #setIgnoredRule}
     *     3.Set up the log scan listener during enhancement, see {@link #setLogListener}
     *     4.Set up a listener for output enhanced bytecode, see {@link #setOutputListener}
     * </pre>
     *
     * @return BufferedAgentBuilder instance
     */
    public static BufferedAgentBuilder build() {
        return new BufferedAgentBuilder().setBootStrapStrategy()
                .setIgnoredRule()
                .setLogListener()
                .setOutputListener();
    }

    /**
     * Set the bytecode enhancement redefinition strategy, as determined by {@link AgentConfig#isReTransformEnable()}
     * <pre>
     *     1.If don't enhance classes loaded by BootStrapClassLoader，use default strategy
     *     {@link AgentBuilder.RedefinitionStrategy#DISABLED}
     *     2.If need to enhance classes loaded by BootStrapClassLoader，use strategy
     *     {@link AgentBuilder.RedefinitionStrategy#RETRANSFORMATION}
     * </pre>
     *
     * @return BufferedAgentBuilder
     */
    private BufferedAgentBuilder setBootStrapStrategy() {
        if (!config.isReTransformEnable()) {
            return this;
        }
        return addAction(builder -> builder.with(AgentBuilder.RedefinitionStrategy.RETRANSFORMATION));
    }

    /**
     * Set ignore rules for scanning
     * <p>Note that the array type, 8 base types, and {@link ServiceClassLoader},{@link FrameworkClassLoader} loaded
     * classes are not enhanced by default and are ignored directly
     * <p>Other classes are ignored if they match one of the prefixes specified in the configuration
     * {@link AgentConfig#getIgnoredPrefixes}
     *
     * @return BufferedAgentBuilder
     */
    private BufferedAgentBuilder setIgnoredRule() {
        return addAction(builder -> builder.ignore(new IgnoredMatcher(config)));
    }

    /**
     * Set the listener to output logs, which is determined by {@link AgentConfig#isShowEnhanceLog()}
     * <p>Use {@link AgentBuilder.Listener.StreamWriting} to convert information to a string and output as a log
     * <p>Note that the buffer used for output will not be released, notice length of a single line of information
     * in {@link AgentBuilder.Listener.StreamWriting}
     *
     * @return BufferedAgentBuilder
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

                    // Logs of Error and Warn levels triggered in Byte-buddy are reported
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
     * Set the listener for outputting enhanced bytecode
     *
     * @return BufferedAgentBuilder
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
     * Add plugin
     *
     * @param plugins PluginDescription set
     * @return BufferedAgentBuilder Wrapper of AgentBuilder
     */
    public BufferedAgentBuilder addPlugins(Iterable<PluginDescription> plugins) {
        return addAction(new BuilderAction() {
            @Override
            public AgentBuilder process(AgentBuilder builder) {
                AgentBuilder newBuilder = builder;
                for (PluginDescription plugin : plugins) {
                    // This must be assigned to the newBuilder, and the operation cannot be repeated on the original
                    // builder, otherwise the operation in the last loop will not take effect
                    newBuilder = newBuilder.type(plugin).transform(plugin);
                }
                return newBuilder;
            }
        });
    }

    /**
     * Add bytecode enhancement based on{@link com.huaweicloud.sermant.core.plugin.agent.declarer.PluginDeclarer}
     *
     * @param pluginDeclarer plugin declarer
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
     * add action
     *
     * @param action action
     * @return BufferedAgentBuilder
     */
    public BufferedAgentBuilder addAction(BuilderAction action) {
        actions.add(action);
        return this;
    }

    /**
     * Build {@link AgentBuilder}，execute all {@link BuilderAction} and execute {@link
     * AgentBuilder#installOn(Instrumentation)}
     *
     * @param instrumentation Instrumentation
     * @return Install result, ResettableClassFileTransformer. If the class metadata is not changed, call the reset
     * method to reset it
     */
    public ResettableClassFileTransformer install(Instrumentation instrumentation) {
        AgentBuilder builder = new Default().disableClassFormatChanges();
        for (BuilderAction action : actions) {
            builder = action.process(builder);
        }
        return builder.installOn(instrumentation);
    }

    /**
     * IgnoredMatcher
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
            if (classLoader instanceof SermantClassLoader) {
                return true;
            }
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
     * Builder action
     *
     * @since 2022-01-22
     */
    public interface BuilderAction {
        /**
         * Execute build
         *
         * @param builder builder
         * @return AgentBuilder
         */
        AgentBuilder process(AgentBuilder builder);
    }
}
