/*
 * Copyright (C) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.apm.core.plugin.service;

import java.util.ServiceLoader;

import com.huawei.apm.core.service.ServiceManager;

/**
 * 插件服务管理器，核心服务管理器{@link ServiceManager}的特化，专门用来初始化{@link PluginService}
 *
 * @author HapThorin
 * @version 1.0.0
 * @since 2021/11/12
 */
public class PluginServiceManager extends ServiceManager {
    public static void initPluginService(ClassLoader classLoader) {
        for (PluginService service : ServiceLoader.load(PluginService.class, classLoader)) {
            if (loadService(service, service.getClass(), PluginService.class)) {
                service.start();
            }
        }
    }
}
