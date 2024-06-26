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

package io.sermant.premain;

import io.sermant.god.common.SermantClassLoader;
import io.sermant.god.common.SermantManager;
import io.sermant.premain.common.AgentArgsResolver;
import io.sermant.premain.common.BootArgsBuilder;
import io.sermant.premain.common.BootConstant;
import io.sermant.premain.common.DirectoryCheckException;
import io.sermant.premain.common.PathDeclarer;
import io.sermant.premain.utils.LoggerUtils;
import io.sermant.premain.utils.StringUtils;

import java.io.File;
import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The Agent startup method, can be premain or agentmain
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
     * @param agentArgs agent arguments by premain
     * @param instrumentation instrumentation during current startup period
     */
    public static void premain(String agentArgs, Instrumentation instrumentation) {
        launchAgent(agentArgs, instrumentation, false);
    }

    /**
     * agentmain
     *
     * @param agentArgs agent arguments by agentmain
     * @param instrumentation instrumentation during current startup period
     */
    public static void agentmain(String agentArgs, Instrumentation instrumentation) {
        launchAgent(agentArgs, instrumentation, true);
    }

    private static void launchAgent(String agentArgs, Instrumentation instrumentation, boolean isDynamic) {
        try {
            // Resolve agent arguments
            final Map<String, String> agentArgsMap = AgentArgsResolver.resolveAgentArgs(agentArgs);

            installGodLibs(instrumentation);

            // Initialize bootstrap arguments via configuration file
            LOGGER.info("Building argument map by agent arguments.");
            String agentPath = agentArgsMap.get(BootConstant.AGENT_PATH_KEY);
            Map<String, Object> bootArgsMap = new HashMap<>(agentArgsMap);
            BootArgsBuilder.build(bootArgsMap, agentPath);

            String artifact = (String) bootArgsMap.get(BootConstant.ARTIFACT_NAME_KEY);

            // Install agent
            installAgent(instrumentation, isDynamic, artifact, bootArgsMap, agentPath);

            // Execute the command in the agent arguments
            executeCommand(artifact, agentArgsMap);
        } catch (Exception exception) {
            LOGGER.log(Level.SEVERE, "Loading sermant agent failed: " + exception.getMessage());
        }
    }

    private static void installAgent(Instrumentation instrumentation, boolean isDynamic, String artifact,
            Map<String, Object> argsMap, String agentPath) {
        try {
            if (!SermantManager.checkSermantStatus(artifact)) {
                // Add core library
                LOGGER.info("Loading core library into SermantClassLoader.");
                SermantClassLoader sermantClassLoader = SermantManager.createSermant(artifact, getCoreLibUrls(
                        agentPath));

                // If the current artifact is not installed, install the agent
                LOGGER.log(Level.INFO, "Loading sermant agent, artifact is: " + artifact);
                sermantClassLoader.loadClass(BootConstant.AGENT_CORE_ENTRANCE_CLASS)
                        .getDeclaredMethod(BootConstant.AGENT_INSTALL_METHOD, String.class, Map.class,
                                Instrumentation.class, boolean.class)
                        .invoke(null, artifact, argsMap, instrumentation, isDynamic);
                LOGGER.log(Level.INFO, "Load sermant done, artifact is: " + artifact);
                SermantManager.updateSermantStatus(artifact, true);
            } else {
                LOGGER.log(Level.INFO, "Sermant for artifact is running, artifact is: " + artifact);
            }
        } catch (InvocationTargetException invocationTargetException) {
            LOGGER.log(Level.SEVERE,
                    "Install agent failed: " + LoggerUtils.recordStackTrace(
                            invocationTargetException.getTargetException()));
        } catch (IOException | IllegalAccessException | NoSuchMethodException | ClassNotFoundException exception) {
            LOGGER.log(Level.SEVERE,
                    "Install agent failed: " + LoggerUtils.recordStackTrace(exception));
        }
    }

    private static void installGodLibs(Instrumentation instrumentation) {
        if (!installFlag) {
            // Add boot library
            LOGGER.info("Loading god library into BootstrapClassLoader.");
            appendGodLibToBootStrapClassLoaderSearch(instrumentation);
            installFlag = true;
        }
    }

    private static void executeCommand(String artifact, Map<String, String> agentArgsMap) {
        // Process command in agent arguments
        String command = agentArgsMap.get(BootConstant.COMMAND_KEY);
        if (command == null || command.isEmpty()) {
            return;
        }
        LOGGER.log(Level.INFO, "Execute command: " + command);
        try {
            SermantClassLoader sermantClassLoader = SermantManager.getSermant(artifact);
            if (sermantClassLoader == null) {
                LOGGER.log(Level.SEVERE,
                        "Execute command failed, sermant has not been installed, artifact is: " + artifact);
                return;
            }
            sermantClassLoader.loadClass(BootConstant.COMMAND_PROCESSOR_CLASS)
                    .getDeclaredMethod(BootConstant.COMMAND_PROCESS_METHOD, Map.class).invoke(null, agentArgsMap);
        } catch (InvocationTargetException invocationTargetException) {
            LOGGER.log(Level.SEVERE,
                    "Execute command failed: " + invocationTargetException.getTargetException().getMessage());
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException exception) {
            LOGGER.log(Level.SEVERE, "Execute command failed: " + exception.getMessage());
        }
    }

    private static void appendGodLibToBootStrapClassLoaderSearch(Instrumentation instrumentation) {
        final File bootstrapDir = new File(PathDeclarer.getGodLibPath(PathDeclarer.getAgentPath()));
        if (!bootstrapDir.exists() || !bootstrapDir.isDirectory()) {
            throw new DirectoryCheckException("God directory is not exist or is not directory.");
        }
        File[] jars = bootstrapDir.listFiles((dir, name) -> name.endsWith(".jar"));
        if (jars == null || jars.length == 0) {
            throw new DirectoryCheckException("God directory is empty");
        }

        for (File jar : jars) {
            try (JarFile jarFile = new JarFile(jar)) {
                instrumentation.appendToBootstrapClassLoaderSearch(jarFile);
            } catch (IOException ioException) {
                LOGGER.severe(ioException.getMessage());
            }
        }
    }

    private static URL[] getCoreLibUrls(String agentPath) throws IOException {
        String realPath = StringUtils.isBlank(agentPath) ? PathDeclarer.getAgentPath() : agentPath;
        final File coreDir = new File(PathDeclarer.getCorePath(realPath));
        if (!coreDir.exists() || !coreDir.isDirectory()) {
            throw new DirectoryCheckException("Core directory is not exist or is not directory.");
        }
        final File[] jars = coreDir.listFiles((dir, name) -> name.endsWith(".jar"));
        if (jars == null || jars.length == 0) {
            throw new DirectoryCheckException("Core directory is empty.");
        }
        List<URL> list = new ArrayList<>();
        for (File jar : jars) {
            list.add(jar.toURI().toURL());
        }
        return list.toArray(new URL[]{});
    }
}