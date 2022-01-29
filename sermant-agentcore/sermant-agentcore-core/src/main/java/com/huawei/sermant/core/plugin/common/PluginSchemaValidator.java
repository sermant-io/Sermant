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

package com.huawei.sermant.core.plugin.common;

import com.huawei.sermant.core.exception.SchemaException;
import com.huawei.sermant.core.utils.JarFileUtils;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarFile;

/**
 * 插件元信息校验器
 *
 * @author HapThorin
 * @version 1.0.0
 * @since 2022-01-22
 */
public class PluginSchemaValidator {
    /**
     * 插件名称与版本的集合
     */
    private static final Map<String, String> PLUGIN_VERSION_MAP = new HashMap<>();

    private PluginSchemaValidator() {
    }

    /**
     * 获取插件名和插件版本的集合
     *
     * @return 插件名和插件版本的集合
     */
    public static Map<String, String> getPluginVersionMap() {
        return Collections.unmodifiableMap(PLUGIN_VERSION_MAP);
    }

    /**
     * 当插件的版本不存在时，使用默认的版本号
     *
     * @param name 插件名称
     */
    public static void setDefaultVersion(String name) {
        if (!PLUGIN_VERSION_MAP.containsKey(name)) {
            PLUGIN_VERSION_MAP.put(name, PluginConstant.PLUGIN_DEFAULT_VERSION);
        }
    }

    /**
     * 检查名称和版本
     *
     * @param name    插件名称
     * @param jarFile 插件包
     * @return 为真时经过名称和版本校验，为插件包或插件服务包，为假时表示第三方jar包
     * @throws IOException 获取manifest文件异常
     */
    public static boolean checkSchema(String name, JarFile jarFile) throws IOException {
        final Object nameAttr = JarFileUtils.getManifestAttr(jarFile, PluginConstant.PLUGIN_NAME_KEY);
        if (nameAttr == null) {
            return false;
        }
        if (!nameAttr.toString().equals(name)) {
            throw new SchemaException(SchemaException.UNEXPECTED_NAME, nameAttr.toString(), name);
        }
        final Object versionAttr = JarFileUtils.getManifestAttr(jarFile, PluginConstant.PLUGIN_VERSION_KEY);
        final String givingVersion =
                versionAttr == null ? PluginConstant.PLUGIN_DEFAULT_VERSION : versionAttr.toString();
        final String expectingVersion = PLUGIN_VERSION_MAP.get(name);
        if (expectingVersion == null) {
            PLUGIN_VERSION_MAP.put(name, givingVersion);
        } else if (!expectingVersion.equals(givingVersion)) {
            throw new SchemaException(SchemaException.UNEXPECTED_VERSION, name, givingVersion, expectingVersion);
        }
        return true;
    }
}
