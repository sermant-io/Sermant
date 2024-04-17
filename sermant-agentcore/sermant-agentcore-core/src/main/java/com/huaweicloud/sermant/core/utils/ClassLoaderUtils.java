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

package com.huaweicloud.sermant.core.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Enumeration;
import java.util.Optional;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * {@link ClassLoader} related tool class
 *
 * @author HapThorin
 * @version 1.0.0
 * @since 2022-01-22
 */
public class ClassLoaderUtils {
    /**
     * buffer size
     */
    private static final int BUFFER_SIZE = 1024 * 16;

    /**
     * class file suffix
     */
    private static final String CLASSFILE_SUFFIX = ".class";

    private ClassLoaderUtils() {
    }

    /**
     * Load additional jar packages into the classloader
     * <p>Try using {@link URLClassLoader}#{@code addURL} method to load, obtain the resource by
     * {@link ClassLoader#getResource(String)}
     * <p>If the classloader is not {@link URLClassLoader}, the {@link #loadJarFile(ClassLoader, JarFile)} method is
     * called to load it
     *
     * @param classLoader classLoader
     * @param jarUrl jar URL
     * @throws NoSuchMethodException The addURL method or defineClass method could not be found and would not be thrown
     * normally
     *
     * @throws InvocationTargetException Error calling addURL method or defineClass method
     * @throws IllegalAccessException The addURL method or defineClass method cannot be accessed, and will not be thrown
     * normally
     *
     * @throws IOException Failed to load the class file from the jar package
     */
    public static void loadJarFile(ClassLoader classLoader, URL jarUrl)
            throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, IOException {
        if (classLoader instanceof URLClassLoader) {
            Method addUrl = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
            AccessController.doPrivileged(new PrivilegedAction<Object>() {
                @Override
                public Object run() {
                    addUrl.setAccessible(true);
                    return Optional.empty();
                }
            });
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
     * Unpack the jar package and add all the class files in it to the classloader
     * <p>Note that this method does not load any resources and cannot get the contents loaded by this method via
     * {@link ClassLoader#getResource(String)}
     *
     * @param classLoader classLoader
     * @param jarFile jar file
     * @throws IOException Failed to load the class file from the jar package
     * @throws InvocationTargetException Error calling defineClass method
     * @throws IllegalAccessException The defineClass method cannot be accessed, will not be thrown normally
     * @throws NoSuchMethodException The defineClass method could not be found, will not be thrown normally
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
     * Reads all bytes in the input stream
     *
     * @param inputStream input stream
     * @return all bytes in input stream
     * @throws IOException read exception
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
     * Gets the bytecode of the class from the classloader
     *
     * @param classLoader classLoader
     * @param clsName Class fully qualified name
     * @return The bytecode of the class
     * @throws IOException Read class failure
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
     * Define the class to the classloader
     *
     * @param className fully qualified name of the defined class
     * @param classLoader classLoader
     * @param bytes bytes
     * @return class object
     * @throws InvocationTargetException Error calling defineClass method
     * @throws IllegalAccessException The defineClass method cannot be accessed, will not be thrown normally
     * @throws NoSuchMethodException The defineClass method could not be found, will not be thrown normally
     */
    public static Class<?> defineClass(String className, ClassLoader classLoader, byte[] bytes)
            throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        final Method loadingLock = ClassLoader.class.getDeclaredMethod("getClassLoadingLock", String.class);
        AccessController.doPrivileged(new PrivilegedAction<Object>() {
            @Override
            public Object run() {
                loadingLock.setAccessible(true);
                return Optional.empty();
            }
        });
        synchronized (loadingLock.invoke(classLoader, className)) {
            final Method defineClass = ClassLoader.class.getDeclaredMethod("defineClass", String.class, byte[].class,
                    int.class, int.class);
            AccessController.doPrivileged(new PrivilegedAction<Object>() {
                @Override
                public Object run() {
                    defineClass.setAccessible(true);
                    return Optional.empty();
                }
            });
            return (Class<?>) defineClass.invoke(classLoader, null, bytes, 0, bytes.length);
        }
    }
}
