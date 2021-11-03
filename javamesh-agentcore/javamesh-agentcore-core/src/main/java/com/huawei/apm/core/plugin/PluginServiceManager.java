package com.huawei.apm.core.plugin;

import com.huawei.apm.core.classloader.PluginClassLoader;
import com.huawei.apm.core.service.PluginService;
import com.huawei.apm.core.classloader.ClassLoaderManager;
import com.huawei.apm.core.lubanops.bootstrap.log.LogFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 插件服务管理器
 */
public enum PluginServiceManager {
    INSTANCE;

    private final AtomicBoolean isInit = new AtomicBoolean(false);

    private final Map<Class<?>, PluginService> pluginServices = new HashMap<Class<?>, PluginService>();

    /**
     * 初始化插件
     *
     * @param classLoader 类加载器
     */
    public void init(ClassLoader classLoader) {
        if (!isInit.get() && isInit.compareAndSet(false, true) && isTargetClassloader(classLoader)) {
            loadAllPluginServices(classLoader);
            addShutdownHook();
        }
    }

    private boolean isTargetClassloader(ClassLoader classLoader) {
        if (classLoader == null) {
            return false;
        }
        final String name = classLoader.getClass().getName();
        return !(name.startsWith("com.huawei.apm") || name.startsWith("com.lubanops.apm"));
    }

    private void loadAllPluginServices(ClassLoader classLoader) {
        Iterable<PluginService> pluginServiceList = PluginClassLoader.load(PluginService.class);
        final ClassLoader targetClassLoader = ClassLoaderManager.getTargetClassLoader(classLoader);
        Thread.currentThread().setContextClassLoader(targetClassLoader);
        for (PluginService service : pluginServiceList) {
            try {
                final PluginService pluginService = (PluginService) Class.forName(service.getClass().getName(), true, targetClassLoader).newInstance();
                pluginService.init();
                pluginServices.put(service.getClass(), service);
            } catch (Exception e) {
                LogFactory.getLogger().warning(String.format("The plugin service {%s} init failed! {%s}",
                        service.getClass().getName(), e.getMessage()));
            }
        }
        Thread.currentThread().setContextClassLoader(classLoader);
    }

    private void stop() {
        Set<Map.Entry<Class<?>, PluginService>> entries = pluginServices.entrySet();
        for (Map.Entry<Class<?>, PluginService> entry : entries) {
            try {
                entry.getValue().stop();
            } catch (Exception e) {
                LogFactory.getLogger().warning(String.format("The plugin service {%s} stop failed! {%s}",
                        entry.getValue().getClass().getName(), e.getMessage()));
            }
        }
    }

    private void addShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                stop();
            }
        }, "Plugin Service Manager Thread"));
    }
}
