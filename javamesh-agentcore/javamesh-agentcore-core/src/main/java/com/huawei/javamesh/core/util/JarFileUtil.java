/*
 * Copyright (C) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved
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

package com.huawei.javamesh.core.util;

import java.io.IOException;
import java.util.jar.Attributes;
import java.util.jar.JarFile;

/**
 * jar包工具类
 *
 * @author HapThorin
 * @version 1.0.0
 * @since 2021/11/29
 */
public class JarFileUtil {
    /**
     * 获取manifest内部属性
     *
     * @param jarFile jar包名称
     * @param key     manifest的键
     * @return manifest内部属性
     * @throws IOException 找不到manifest文件
     */
    public static Object getManifestAttr(JarFile jarFile, String key) throws IOException {
        return jarFile.getManifest().getMainAttributes().get(new Attributes.Name(key));
    }
}
