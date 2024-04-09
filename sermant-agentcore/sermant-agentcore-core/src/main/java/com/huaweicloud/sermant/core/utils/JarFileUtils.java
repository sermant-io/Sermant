/*
 * Copyright (C) 2021-2024 Sermant Authors. All rights reserved.
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

package com.huaweicloud.sermant.core.utils;

import java.io.IOException;
import java.net.URL;
import java.util.jar.Attributes;
import java.util.jar.JarFile;

/**
 * jar package tool class
 *
 * @author HapThorin
 * @version 1.0.0
 * @since 2021-11-29
 */
public class JarFileUtils {
    private JarFileUtils() {
    }

    /**
     * Gets the manifest internal attributes
     *
     * @param jarFile jar package name
     * @param key manifest key
     * @return attributes of manifest
     * @throws IOException can not find manifest
     */
    public static Object getManifestAttr(JarFile jarFile, String key) throws IOException {
        return jarFile.getManifest().getMainAttributes().get(new Attributes.Name(key));
    }

    /**
     * Gets the URL of the jar package the class is in
     *
     * @param cls class
     * @return url
     */
    public static URL getJarUrl(Class<?> cls) {
        return cls.getProtectionDomain().getCodeSource().getLocation();
    }
}
