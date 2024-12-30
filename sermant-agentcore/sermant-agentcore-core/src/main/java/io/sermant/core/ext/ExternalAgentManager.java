/*
 * Copyright (C) 2024-2024 Sermant Authors. All rights reserved.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package io.sermant.core.ext;

import io.sermant.core.classloader.FrameworkClassLoader;
import io.sermant.core.common.CommonConstant;
import io.sermant.core.common.LoggerFactory;
import io.sermant.core.event.collector.FrameworkEventCollector;
import io.sermant.core.event.collector.FrameworkEventDefinitions;
import io.sermant.core.exception.SermantRuntimeException;
import io.sermant.core.ext.otel.OtelConstant;
import io.sermant.core.plugin.classloader.PluginClassLoader;
import io.sermant.core.plugin.classloader.ServiceClassLoader;
import io.sermant.core.utils.FileUtils;
import io.sermant.god.common.SermantClassLoader;

import java.io.File;
import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The manager of external agent, mainly for installation
 *
 * @author lilai
 * @since 2024-12-14
 */
public class ExternalAgentManager {
    private static final Logger LOGGER = LoggerFactory.getLogger();

    private static final Map<String, AtomicBoolean> EXTERNAL_AGENT_INSTALLATION_STATUS = new HashMap<>();

    private static final Map<String, String> EXTERNAL_AGENT_VERSION = new HashMap<>();

    private static final String AGENTMAIN = "agentmain";

    private static final String PREMAIN = "premain";

    private static final String PREMAIN_CLASS = "Premain-Class";

    private static final String IMPLEMENTATION_VERSION = "Implementation-Version";

    private static final String DEFAULT_AGENT_VERSION = "unknown";

    private ExternalAgentManager() {
    }

    /**
     * get agent version
     *
     * @param agentName agent name
     * @return agent version
     */
    public static String getAgentVersion(String agentName) {
        return EXTERNAL_AGENT_VERSION.getOrDefault(agentName, DEFAULT_AGENT_VERSION);
    }

    /**
     * set agent version
     *
     * @param agentName agent name
     * @param agentVersion agent version
     */
    public static void setAgentVersion(String agentName, String agentVersion) {
        EXTERNAL_AGENT_VERSION.put(agentName, agentVersion);
    }

    /**
     * get the status of specific agent
     *
     * @param agentName agent name
     * @return status
     */
    public static boolean getInstallationStatus(String agentName) {
        AtomicBoolean atomicBoolean = EXTERNAL_AGENT_INSTALLATION_STATUS.get(agentName);
        return atomicBoolean != null && atomicBoolean.get();
    }

    /**
     * get the status of all agents
     *
     * @return status map
     */
    public static Map<String, AtomicBoolean> getExternalAgentInstallationStatus() {
        return EXTERNAL_AGENT_INSTALLATION_STATUS;
    }

    /**
     * Install OpenTelemetry Agent
     *
     * @param isDynamic Whether the installation is dynamic
     * @param agentName agent name
     * @param agentPath OpenTelemetry agent file path
     * @param argsMap arguments of the installation
     * @param instrumentation instrumentation
     * @throws IOException
     * @throws NoSuchMethodException
     * @throws ClassNotFoundException
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     */
    public static void installExternalAgent(boolean isDynamic, String agentName, String agentPath,
            Map<String, String> argsMap, Instrumentation instrumentation) throws IOException,
            NoSuchMethodException, ClassNotFoundException, InvocationTargetException, IllegalAccessException {
        AtomicBoolean agentStatus = EXTERNAL_AGENT_INSTALLATION_STATUS.computeIfAbsent(agentName,
                k -> new AtomicBoolean(false));
        if (agentStatus.get()) {
            LOGGER.log(Level.WARNING, "{0} agent is already installed. Only one agent can be installed at a time.",
                    agentName);
            return;
        }

        installAgent(isDynamic, agentName, agentPath, argsMap, instrumentation);
        agentStatus.set(true);
        LOGGER.log(Level.INFO, "{0} agent installed successfully.", agentName);
        FrameworkEventCollector.getInstance()
                .collectdHotPluggingEvent(FrameworkEventDefinitions.EXTERNAL_AGENT_INSTALL,
                        "Hot plugging command[INSTALL-EXTERNAL-AGENT] has been processed. Agent name is: " + agentName);
    }

    static void installAgent(boolean isDynamic, String agentName, String agentPath,
            Map<String, String> argsMap, Instrumentation instrumentation) throws IOException,
            ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        String agentClassName = loadAgentJar(agentName, agentPath, instrumentation);
        Class<?> clazz = ClassLoader.getSystemClassLoader().loadClass(agentClassName);
        Method method;
        if (isDynamic) {
            method = clazz.getMethod(AGENTMAIN, String.class, Instrumentation.class);
            setArgsToSystemProperties(argsMap);
        } else {
            method = clazz.getMethod(PREMAIN, String.class, Instrumentation.class);
        }

        // specially support OTEL
        if (OtelConstant.OTEL.equals(agentName)) {
            initializeOtelArgsProperties();
        }
        method.invoke(null, "", instrumentation);
    }

    /**
     * Load external agent jar
     *
     * @param agentPath file path of external agent
     * @param instrumentation instrumentation
     * @return entrance class of external agent
     * @throws IOException
     */
    static String loadAgentJar(String agentName, String agentPath, Instrumentation instrumentation)
            throws IOException {
        File agentJarFile = new File(agentPath);
        if (!agentJarFile.isFile()) {
            throw new SermantRuntimeException("Invalid Jar file: " + agentJarFile);
        }

        String externalAgentClassName;
        try (JarFile jarFile = new JarFile(agentPath)) {
            instrumentation.appendToSystemClassLoaderSearch(new JarFile(agentJarFile));
            Attributes attributes = FileUtils.getJarFileAttributes(jarFile);
            setAgentVersion(agentName, attributes.getValue(IMPLEMENTATION_VERSION));
            externalAgentClassName = attributes.getValue(PREMAIN_CLASS);
        }
        return externalAgentClassName;
    }

    /**
     * Set system properties for external agent in dynamic installation scenario, like otel.javaagent.debug=true
     *
     * @param argsMap arguments of dynamic installation
     */
    static void setArgsToSystemProperties(Map<String, String> argsMap) {
        for (Entry<String, String> entry : argsMap.entrySet()) {
            System.setProperty(entry.getKey(), entry.getValue());
        }
    }

    /**
     * Initialize necessary OpenTelemetry agent properties to avoid conflicts between Sermant and OpenTelemetry
     */
    static void initializeOtelArgsProperties() {
        System.setProperty(OtelConstant.OTEL_JAVAAGENT_EXCLUDE_CLASS_LOADERS,
                FrameworkClassLoader.class.getName() + CommonConstant.COMMA + SermantClassLoader.class.getName()
                        + CommonConstant.COMMA + PluginClassLoader.class.getName() + CommonConstant.COMMA
                        + ServiceClassLoader.class.getName());
        System.setProperty(OtelConstant.OTEL_JAVAAGENT_EXCLUDE_CLASSES, OtelConstant.IO_SERMANT_PREFIX);
    }
}
