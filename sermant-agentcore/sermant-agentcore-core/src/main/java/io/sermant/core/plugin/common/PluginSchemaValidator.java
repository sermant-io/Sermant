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

package io.sermant.core.plugin.common;

import io.sermant.core.exception.SchemaException;
import io.sermant.core.utils.JarFileUtils;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarFile;

/**
 * Plugin scheme validator
 *
 * @author HapThorin
 * @version 1.0.0
 * @since 2022-01-22
 */
public class PluginSchemaValidator {
    /**
     * A collection of plugin names and versions
     */
    private static final Map<String, String> PLUGIN_VERSION_MAP = new HashMap<>();

    private PluginSchemaValidator() {
    }

    /**
     * Gets PLUGIN_VERSION_MAP
     *
     * @return A collection of plugin names and plug-in versions
     */
    public static Map<String, String> getPluginVersionMap() {
        return Collections.unmodifiableMap(PLUGIN_VERSION_MAP);
    }

    /**
     * When the version of the plugin does not exist, the default version number is used
     *
     * @param pluginName plugin name
     */
    public static void setDefaultVersion(String pluginName) {
        if (!PLUGIN_VERSION_MAP.containsKey(pluginName)) {
            PLUGIN_VERSION_MAP.put(pluginName, PluginConstant.PLUGIN_DEFAULT_VERSION);
        }
    }

    /**
     * Remove plugin version cache
     *
     * @param pluginName 插件名
     */
    public static void removePluginVersionCache(String pluginName) {
        PLUGIN_VERSION_MAP.remove(pluginName);
    }

    /**
     * Check the name and version
     *
     * @param pluginName plugin name
     * @param realPluginName The actual plugin name, the plugin name used after the replica tag is removed
     * @param jarFile plugin package
     * @return If true, after name and version verification, it is a plugin package or plugin service package. If false
     * means the third-party jar package
     *
     * @throws IOException Get the manifest file exception
     * @throws SchemaException The plugin name does not match the one retrieved from the resource file
     */
    public static boolean checkSchema(String pluginName, String realPluginName, JarFile jarFile) throws IOException {
        final Object nameAttr = JarFileUtils.getManifestAttr(jarFile, PluginConstant.PLUGIN_NAME_KEY);
        if (nameAttr == null) {
            return false;
        }
        if (!nameAttr.toString().equals(realPluginName)) {
            throw new SchemaException(SchemaException.UNEXPECTED_NAME, nameAttr.toString(), pluginName);
        }
        final Object versionAttr = JarFileUtils.getManifestAttr(jarFile, PluginConstant.PLUGIN_VERSION_KEY);
        final String givingVersion =
                versionAttr == null ? PluginConstant.PLUGIN_DEFAULT_VERSION : versionAttr.toString();
        final String expectingVersion = PLUGIN_VERSION_MAP.get(pluginName);
        if (expectingVersion == null) {
            PLUGIN_VERSION_MAP.put(pluginName, givingVersion);
        } else if (!expectingVersion.equals(givingVersion)) {
            throw new SchemaException(SchemaException.UNEXPECTED_VERSION, pluginName, givingVersion, expectingVersion);
        }
        return true;
    }
}
