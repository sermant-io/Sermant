/*
 * Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huaweicloud.sermant.premain;

import com.huaweicloud.sermant.god.common.SermantClassLoader;
import com.huaweicloud.sermant.god.common.SermantManager;
import com.huaweicloud.sermant.premain.common.BootArgsBuilder;
import com.huaweicloud.sermant.premain.common.BootConstant;
import com.huaweicloud.sermant.premain.common.PathDeclarer;
import com.huaweicloud.sermant.premain.utils.LoggerUtils;

import java.io.File;
import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Agent Premain方法
 *
 * @author luanwenfei
 * @since 2022-03-26
 */
public class AgentLauncher {
    private static final Logger LOGGER = LoggerUtils.getLogger();

    private static boolean installFlag = false;

    private AgentLauncher() {
    }

    /**
     * premain
     *
     * @param agentArgs premain启动时携带的参数
     * @param instrumentation 本次启动使用的instrumentation
     */
    public static void premain(String agentArgs, Instrumentation instrumentation) {
        launchAgent(agentArgs, instrumentation, false);
    }

    /**
     * agentmain
     *
     * @param agentArgs agentmain启动时携带的参数
     * @param instrumentation 本次启动使用的instrumentation
     */
    public static void agentmain(String agentArgs, Instrumentation instrumentation) {
        launchAgent(agentArgs, instrumentation, true);
    }

    private static void launchAgent(String agentArgs, Instrumentation instrumentation, boolean isDynamic) {
        try {
            if (!installFlag) {
                // 添加引导库
                LOGGER.info("Loading god library into BootstrapClassLoader.");
                loadGodLib(instrumentation);
                installFlag = true;
            }

            // 初始化启动参数
            LOGGER.info("Building argument map by agent arguments.");
            final Map<String, Object> argsMap = BootArgsBuilder.build(agentArgs);
            String artifact = (String) argsMap.get(BootConstant.ARTIFACT_NAME_KEY);

            if (SermantManager.checkSermantStatus(artifact)) {
                LOGGER.log(Level.WARNING, "Sermant for artifact is running，artifact is: " + artifact);
                return;
            }

            // 添加核心库
            LOGGER.info("Loading core library into SermantClassLoader.");
            SermantClassLoader sermantClassLoader = SermantManager.createSermant(artifact, loadCoreLibUrls());

            // agent core入口
            LOGGER.log(Level.INFO, "Loading sermant agent, artifact is: " + artifact);
            sermantClassLoader.loadClass("com.huaweicloud.sermant.core.AgentCoreEntrance")
                    .getDeclaredMethod("install", String.class, Map.class, Instrumentation.class, boolean.class)
                    .invoke(null, artifact, argsMap, instrumentation, isDynamic);
            LOGGER.log(Level.INFO, "Load sermant done， artifact is: " + artifact);
            SermantManager.updateSermantStatus(artifact, true);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Loading sermant agent failed.", e);
        }
    }

    private static URL[] loadCoreLibUrls() throws IOException {
        final File coreDir = new File(PathDeclarer.getCorePath());
        if (!coreDir.exists() || !coreDir.isDirectory()) {
            throw new RuntimeException("Core directory is not exist or is not directory.");
        }
        final File[] jars = coreDir.listFiles((dir, name) -> name.endsWith(".jar"));
        if (jars == null || jars.length == 0) {
            throw new RuntimeException("Core directory is empty.");
        }
        List<URL> list = new ArrayList<>();
        for (File jar : jars) {
            list.add(jar.toURI().toURL());
        }
        return list.toArray(new URL[]{});
    }

    private static void loadGodLib(Instrumentation instrumentation) throws IOException {
        final File bootstrapDir = new File(PathDeclarer.getGodLibPath());
        if (!bootstrapDir.exists() || !bootstrapDir.isDirectory()) {
            throw new RuntimeException("God directory is not exist or is not directory.");
        }
        File[] jars = bootstrapDir.listFiles((dir, name) -> name.endsWith(".jar"));
        if (jars == null || jars.length == 0) {
            throw new RuntimeException("God directory is empty");
        }

        for (File jar : jars) {
            try (JarFile jarFile = new JarFile(jar)) {
                instrumentation.appendToBootstrapClassLoaderSearch(jarFile);
            } catch (IOException ioException) {
                LOGGER.severe(ioException.getMessage());
            }
        }
    }
}