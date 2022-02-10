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

package com.huawei.sermant.core.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * {@link ClassLoader}相关的工具类
 *
 * @author HapThorin
 * @version 1.0.0
 * @since 2022-01-22
 */
public class ClassLoaderUtils {
    /**
     * 缓冲区大小
     */
    private static final int BUFFER_SIZE = 1024 * 16;

    /**
     * class文件后缀名
     */
    private static final String CLASSFILE_SUFFIX = ".class";

    private ClassLoaderUtils() {
    }

    /**
     * 加载额外jar包至类加载器中
     * <p>先尝试使用{@link URLClassLoader}#{@code addURL}方法加载，可通过{@link ClassLoader#getResource(String)}获取资源
     * <p>若类加载不是{@link URLClassLoader}，则调用{@link #loadJarFile(ClassLoader, JarFile)}方法加载
     *
     * @param classLoader 类加载器
     * @param jarUrl      jar包URL
     * @throws NoSuchMethodException     无法找到addURL方法或defineClass方法，正常不会报出
     * @throws InvocationTargetException 调用addURL方法或defineClass方法错误
     * @throws IllegalAccessException    无法访问addURL方法或defineClass方法，正常不会报出
     * @throws IOException               从jar包中加载class文件失败
     */
    public static void loadJarFile(ClassLoader classLoader, URL jarUrl)
            throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, IOException {
        if (classLoader instanceof URLClassLoader) {
            Method addUrl = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
            addUrl.setAccessible(true);
            addUrl.invoke(classLoader, jarUrl);
        } else {
            JarFile jarFile = null;
            try {
                jarFile = new JarFile(jarUrl.getPath());
                loadJarFile(classLoader, jarFile);
            } finally {
                if (jarFile != null) {
                    jarFile.close();
                }
            }
        }
    }

    /**
     * 拆解jar包并将其中所有class文件加入到类加载器中
     * <p>注意，该方法不会加载任何resource，无法通过{@link ClassLoader#getResource(String)}获取本方法加载的内容
     *
     * @param classLoader 类加载器
     * @param jarFile     jar包
     * @throws IOException               从jar包中加载class文件失败
     * @throws InvocationTargetException 调用defineClass方法错误
     * @throws IllegalAccessException    无法访问defineClass方法，正常不会报出
     * @throws NoSuchMethodException     无法找到defineClass方法，正常不会报出
     */
    public static void loadJarFile(ClassLoader classLoader, JarFile jarFile)
            throws IOException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        final Enumeration<JarEntry> entry = jarFile.entries();
        while (entry.hasMoreElements()) {
            final JarEntry jarEntry = entry.nextElement();
            if (jarEntry.isDirectory()) {
                continue;
            }
            final String entryName = jarEntry.getName();
            if (!entryName.endsWith(CLASSFILE_SUFFIX)) {
                continue;
            }
            InputStream inputStream = null;
            try {
                inputStream = jarFile.getInputStream(jarEntry);
                defineClass(entryName.substring(0, entryName.length() - CLASSFILE_SUFFIX.length()).replace('/', '.'),
                        classLoader, readBytes(inputStream));
            } finally {
                if (inputStream != null) {
                    inputStream.close();
                }
            }
        }
    }

    /**
     * 读取输入流中所有字节
     *
     * @param inputStream 输入流
     * @return 输入流中所有字节
     * @throws IOException 读取失败
     */
    private static byte[] readBytes(InputStream inputStream) throws IOException {
        int count = inputStream.available();
        if (count == 0) {
            ByteArrayOutputStream byteStream = null;
            try {
                byteStream = new ByteArrayOutputStream();
                byte[] bytes = new byte[BUFFER_SIZE];
                int size;
                while ((size = inputStream.read(bytes)) >= 0) {
                    byteStream.write(bytes, 0, size);
                }
                return byteStream.size() > 0 ? byteStream.toByteArray() : new byte[0];
            } finally {
                if (byteStream != null) {
                    byteStream.close();
                }
            }
        } else {
            byte[] bytes = new byte[count];
            return inputStream.read(bytes) >= 0 ? bytes : new byte[0];
        }
    }

    /**
     * 从类加载器中获取类的字节码
     *
     * @param classLoader 类加载器
     * @param clsName     类全限定名
     * @return 类的字节码
     * @throws IOException 读取类失败
     */
    public static byte[] getClassResource(ClassLoader classLoader, String clsName) throws IOException {
        InputStream inputStream = null;
        try {
            inputStream = classLoader.getResourceAsStream(clsName.replace('.', '/') + CLASSFILE_SUFFIX);
            return inputStream == null ? new byte[0] : readBytes(inputStream);
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
        }
    }

    /**
     * 定义类到类加载器中
     *
     * @param className   定义类的全限定名
     * @param classLoader 类加载器
     * @param bytes       类字节码
     * @return 类对象
     * @throws InvocationTargetException 调用defineClass方法错误
     * @throws IllegalAccessException    无法访问defineClass方法，正常不会报出
     * @throws NoSuchMethodException     无法找到defineClass方法，正常不会报出
     */
    public static Class<?> defineClass(String className, ClassLoader classLoader, byte[] bytes)
            throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        final Method loadingLock = ClassLoader.class.getDeclaredMethod("getClassLoadingLock", String.class);
        loadingLock.setAccessible(true);
        synchronized (loadingLock.invoke(classLoader, className)) {
            final Method defineClass = ClassLoader.class.getDeclaredMethod("defineClass", String.class, byte[].class,
                    int.class, int.class);
            defineClass.setAccessible(true);
            return (Class<?>) defineClass.invoke(classLoader, null, bytes, 0, bytes.length);
        }

    }
}
