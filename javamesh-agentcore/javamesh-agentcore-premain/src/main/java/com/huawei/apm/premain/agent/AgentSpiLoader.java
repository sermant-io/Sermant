package com.huawei.apm.premain.agent;

import com.huawei.apm.premain.classloader.PluginClassLoader;

import java.util.ServiceLoader;

/**
 * 插件spi加载工具
 */
public class AgentSpiLoader {
    public static <T> ServiceLoader<T> load(Class<T> clazz) {
        return ServiceLoader.load(clazz, PluginClassLoader.getDefault());
    }
}
