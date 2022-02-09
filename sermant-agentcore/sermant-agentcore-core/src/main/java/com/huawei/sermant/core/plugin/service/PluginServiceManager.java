/*
 * Copyright (C) 2021-2021 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huawei.sermant.core.plugin.service;

import com.huawei.sermant.core.service.ServiceManager;

import java.util.ServiceLoader;

/**
 * 插件服务管理器，核心服务管理器{@link ServiceManager}的特化，专门用来初始化{@link PluginService}
 *
 * @author HapThorin
 * @version 1.0.0
 * @since 2021-11-12
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
