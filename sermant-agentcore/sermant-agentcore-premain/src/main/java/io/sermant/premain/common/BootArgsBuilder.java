/*
 * Copyright (C) 2021-2021 Huawei Technologies Co., Ltd. All rights reserved.
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

package io.sermant.premain.common;

import io.sermant.premain.utils.LoggerUtils;
import io.sermant.premain.utils.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * Bootstrap Arguments Builder
 *
 * @author HapThorin
 * @version 1.0.0
 * @since 2021-11-12
 */
public abstract class BootArgsBuilder {
    private static final Logger LOGGER = LoggerUtils.getLogger();

    /**
     * Build bootstrap arguments
     *
     * @param argsMap argument map
     * @param agentPath agent path
     */
    public static void build(Map<String, Object> argsMap, String agentPath) {
        final Properties configMap = loadConfig(agentPath);
        addNotNullEntries(argsMap, configMap);
        addNormalEntries(argsMap, configMap);
        addPathEntries(argsMap, agentPath);
    }

    /**
     * Read bootstrap arguments
     *
     * @param agentPath agent path
     * @return bootstrap arguments
     */
    private static Properties loadConfig(String agentPath) {
        String realPath = StringUtils.isBlank(agentPath) ? PathDeclarer.getAgentPath() : agentPath;
        final Properties properties = new Properties();
        try (InputStream configStream = Files.newInputStream(
                Paths.get(PathDeclarer.getBootConfigPath(realPath)))) {
            properties.load(configStream);
        } catch (IOException ioException) {
            LOGGER.severe(String.format(Locale.ROOT, "Exception occurs when close config InputStream , %s .",
                    ioException.getMessage()));
        }
        return properties;
    }

    /**
     * Parameter values are generally obtained from configurations and then from environment variables
     *
     * @param key Parameter key
     * @param configMap configuration
     * @return Parameter value
     */
    private static String getCommonValue(String key, Properties configMap) {
        String value = configMap.getProperty(key);
        return getActualValue(value);
    }

    /**
     * Add a non-empty key-value pair. The parameters involved cannot be empty: appName, serviceName, instanceName, and
     * appType
     *
     * @param argsMap arguments map
     * @param configMap configuration map
     */
    private static void addNotNullEntries(Map<String, Object> argsMap, Properties configMap) {
        String key = BootConstant.ARTIFACT_NAME_KEY;
        String defaultValue = "default";
        if (!argsMap.containsKey(key)) {
            final String value = getCommonValue(key, configMap);
            argsMap.put(key, value == null ? defaultValue : value);
        }
        key = BootConstant.APP_NAME_KEY;
        if (!argsMap.containsKey(key)) {
            final String value = getCommonValue(key, configMap);
            argsMap.put(key, value == null ? defaultValue : value);
        }
        key = BootConstant.SERVICE_NAME_KEY;
        if (!argsMap.containsKey(key)) {
            final String value = getCommonValue(key, configMap);
            argsMap.put(key, value == null ? defaultValue : value);
        }
        key = BootConstant.APP_TYPE_KEY;
        if (!argsMap.containsKey(key)) {
            final String value = getCommonValue(key, configMap);
            argsMap.put(key, value == null ? defaultValue : value);
        }
    }

    /**
     * Add a normal key-value pair. If the value is empty, the key-value pair is not added
     *
     * @param argsMap arguments map
     * @param configMap configuration map
     */
    private static void addNormalEntries(Map<String, Object> argsMap, Properties configMap) {
        for (Object key : configMap.keySet()) {
            if (!argsMap.containsKey((String) key)) {
                final String value = configMap.getProperty((String) key);
                if (value != null) {
                    argsMap.put((String) key, getActualValue(value));
                }
            }
        }
    }

    /**
     * Gets a configuration, environment variable, or system variable in the shape of "${}"
     *
     * @param configVal Configuration value
     * @return Real configuration
     */
    private static String getActualValue(String configVal) {
        if (configVal != null && configVal.matches("^.*\\$\\{[\\w.]+(:.*)?}.*$")) {
            final int startIndex = configVal.indexOf("${") + 2;
            final int endIndex = configVal.indexOf('}', startIndex);
            final String envKey = configVal.substring(startIndex, endIndex);
            final int separatorIndex = envKey.indexOf(':');
            final String key = separatorIndex >= 0 ? envKey.substring(0, separatorIndex) : envKey;
            final String defaultValue = separatorIndex >= 0 ? envKey.substring(separatorIndex + 1) : "";

            // The priority is environment variables > system variables
            if (!StringUtils.isBlank(System.getenv(key))) {
                return System.getenv(key);
            }
            if (!StringUtils.isBlank(System.getProperty(key))) {
                return System.getProperty(key);
            }
            return defaultValue;
        }
        return configVal;
    }

    /**
     * Add a path key-value pair
     *
     * @param argsMap arguments map
     * @param agentPath agent path
     */
    public static void addPathEntries(Map<String, Object> argsMap, String agentPath) {
        // If the specified agentPath is empty, obtain it through the ProtectionDomain
        String realPath = StringUtils.isBlank(agentPath) ? PathDeclarer.getAgentPath() : agentPath;
        argsMap.put(BootConstant.AGENT_ROOT_DIR_KEY, realPath);
        argsMap.put(BootConstant.CORE_IMPLEMENT_DIR_KEY, PathDeclarer.getImplementPath(realPath));
        argsMap.put(BootConstant.CORE_CONFIG_FILE_KEY, PathDeclarer.getConfigPath(realPath));
        argsMap.put(BootConstant.PLUGIN_SETTING_FILE_KEY, PathDeclarer.getPluginSettingPath(realPath));
        argsMap.put(BootConstant.PLUGIN_PACKAGE_DIR_KEY, PathDeclarer.getPluginPackagePath(realPath));
        argsMap.put(BootConstant.LOG_SETTING_FILE_KEY, PathDeclarer.getLogbackSettingPath(realPath));
        argsMap.put(BootConstant.COMMON_DEPENDENCY_DIR_KEY, PathDeclarer.getCommonLibPath(realPath));
    }
}
