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

package com.huawei.sermant.plugins.luban.adaptor.service;

import com.huawei.sermant.core.plugin.adaptor.service.AdaptorService;
import com.huawei.sermant.core.plugin.classloader.PluginClassLoader;

import com.lubanops.apm.premain.AgentPremain;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.matcher.ElementMatchers;

import java.io.File;
import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.util.jar.JarFile;

/**
 * luban适配器服务
 *
 * @author HapThorin
 * @version 1.0.0
 * @since 2022-01-26
 */
public class LubanAdaptorService implements AdaptorService {
    @Override
    public boolean start(String agentMainArg, File execEnvDir, ClassLoader classLoader,
            Instrumentation instrumentation) {
        if (!loadAgentJars(execEnvDir, classLoader, instrumentation)) {
            return false;
        }
        invokePremain(agentMainArg, instrumentation);
        return true;
    }

    @Override
    public void stop() {
    }

    /**
     * 调用入口包的入口方法
     * <p>调用之前，使用byte-buddy修改luban代码，将执行增强的逻辑屏蔽，并将其监听器抽取出来，适配为插件描述器
     *
     * @param agentMainArg    luban agent启动参数
     * @param instrumentation Instrumentation对象
     */
    private void invokePremain(String agentMainArg, Instrumentation instrumentation) {
        new AgentBuilder.Default(new ByteBuddy())
                .with(AgentBuilder.RedefinitionStrategy.RETRANSFORMATION)
                .type(ElementMatchers.named("com.lubanops.apm.core.transformer.TransformerManager"))
                .transform((builder, typeDescription, classLoader, javaModule) -> builder
                        .method(ElementMatchers.named("init"))
                        .intercept(Advice.to(LubanTransformerAdviser.class)))
                .installOn(instrumentation);
        AgentPremain.premain(agentMainArg, instrumentation);
    }

    /**
     * 加载luban入口包
     *
     * @param execEnvDir      运行环境目录
     * @param classLoader     加载适配包的类加载器
     * @param instrumentation Instrumentation对象
     * @return 是否加载入口包成功
     */
    private boolean loadAgentJars(File execEnvDir, ClassLoader classLoader, Instrumentation instrumentation) {
        try {
            final File agentJar = new File(execEnvDir.getCanonicalPath() + File.separatorChar + "apm-javaagent.jar");
            loadAgentJar(agentJar, classLoader, instrumentation);
            return true;
        } catch (IOException ignored) {
            return false;
        }
    }

    /**
     * 加载agent入口包
     *
     * @param agentJar        agent入口
     * @param classLoader     加载适配包的类加载器
     * @param instrumentation Instrumentation对象
     * @throws IOException 加载失败
     */
    private void loadAgentJar(File agentJar, ClassLoader classLoader, Instrumentation instrumentation)
            throws IOException {
        if (classLoader instanceof PluginClassLoader) {
            ((PluginClassLoader) classLoader).addURL(agentJar.toURI().toURL());
        } else {
            JarFile jarfile = null;
            try {
                jarfile = new JarFile(agentJar);
                instrumentation.appendToSystemClassLoaderSearch(jarfile);
            } finally {
                if (jarfile != null) {
                    jarfile.close();
                }
            }
        }
    }
}
