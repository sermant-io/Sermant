/*
 * Copyright (C) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.javamesh.core.plugin.service;

import java.util.ServiceLoader;

import com.huawei.javamesh.core.service.ServiceManager;

/**
 * 插件服务管理器，核心服务管理器{@link ServiceManager}的特化，专门用来初始化{@link PluginService}
 *
 * @author HapThorin
 * @version 1.0.0
 * @since 2021/11/12
 */
public class PluginServiceManager extends ServiceManager {
    /**
     * 初始化插件服务
     *
     * @param classLoader 插件服务包的ClassLoader
     */
    public static void initPluginService(ClassLoader classLoader) {
        for (PluginService service : ServiceLoader.load(PluginService.class, classLoader)) {
            if (loadService(service, service.getClass(), PluginService.class)) {
                service.start();
            }
        }
    }

    /**
     * 获取插件服务
     *
     * @param serviceClass 插件服务类
     * @param <T>          插件服务类型
     * @return 插件服务实例
     */
    public static <T extends PluginService> T getPluginService(Class<T> serviceClass) {
        return getService(serviceClass);
    }
}
