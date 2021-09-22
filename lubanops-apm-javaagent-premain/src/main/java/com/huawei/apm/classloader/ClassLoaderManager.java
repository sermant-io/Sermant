package com.huawei.apm.classloader;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * agent 类管理器
 */
public class ClassLoaderManager {
    /**
     * key : 应用类加载器
     * value: 插件类加载器
     */
    private static final Map<ClassLoader, ClassLoader> PLUGIN_CLASSLOADER_CACHE = new ConcurrentHashMap<ClassLoader, ClassLoader>();

    private static final ReentrantLock CLASSLOADER_LOCK = new ReentrantLock();

    /**
     * 对目标ClassLoader进行转换，使插件加载器继承目标类加载器
     *
     * @param classLoader 目标类加载器，即应用类加载器
     * @return 转换后了加载器
     */
    public static ClassLoader getTargetClassLoader(ClassLoader classLoader) {
        ClassLoader cachePluginClassLoader = PLUGIN_CLASSLOADER_CACHE.get(classLoader);
        if (cachePluginClassLoader == null) {
            try {
                CLASSLOADER_LOCK.lock();
                cachePluginClassLoader = AccessController.doPrivileged(new ClassLoaderPrivilegedAction(classLoader));
                PLUGIN_CLASSLOADER_CACHE.put(classLoader, cachePluginClassLoader);
            } finally {
                CLASSLOADER_LOCK.unlock();
            }
        }
        return cachePluginClassLoader;
    }

    static class ClassLoaderPrivilegedAction implements PrivilegedAction<PluginClassLoader> {
        private final ClassLoader parent;

        ClassLoaderPrivilegedAction(ClassLoader parent) {
            this.parent = parent;
        }

        @Override
        public PluginClassLoader run() {
            return new PluginClassLoader(parent);
        }
    }
}
