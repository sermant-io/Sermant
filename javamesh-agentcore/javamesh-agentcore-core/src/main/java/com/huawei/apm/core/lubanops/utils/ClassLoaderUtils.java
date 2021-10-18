package com.huawei.apm.core.lubanops.utils;

/**
 * Class loader utils.
 *
 * @author
 */
public class ClassLoaderUtils {

    /**
     * push new class loader into thread context
     *
     * @param nextClassLoader new classLoader
     * @return old classloader
     */
    public static ClassLoader pushContextClassLoader(ClassLoader nextClassLoader) {
        ClassLoader preClassLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(nextClassLoader);
        return preClassLoader;
    }

    /**
     * roll back classLoader
     *
     * @param preClassLoader old classLoader
     */
    public static void popContextClassLoader(ClassLoader preClassLoader) {
        Thread.currentThread().setContextClassLoader(preClassLoader);
    }

}
