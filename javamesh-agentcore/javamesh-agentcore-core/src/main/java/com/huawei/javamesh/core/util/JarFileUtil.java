/*
 * Copyright (C) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
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
