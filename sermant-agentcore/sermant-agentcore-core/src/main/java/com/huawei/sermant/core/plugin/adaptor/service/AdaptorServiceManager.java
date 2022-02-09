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

package com.huawei.sermant.core.plugin.adaptor.service;

import java.io.File;
import java.lang.instrument.Instrumentation;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.ServiceLoader;

/**
 * 适配器服务管理器，用于带起适配器的所有{@link AdaptorService}
 *
 * @author HapThorin
 * @version 1.0.0
 * @since 2022-01-26
 */
public class AdaptorServiceManager {
    /**
     * 适配器服务集，用于去重和添加终止钩子
     */
    private static final Map<Class<?>, AdaptorService> SERVICES = new LinkedHashMap<>();

    private AdaptorServiceManager() {
    }

    /**
     * 加载所有的适配器服务，并初始化
     *
     * @param agentMainArg    外部agent启动参数
     * @param execEnvDir      适配器运行环境
     * @param classLoader     加载适配包的类加载器
     * @param instrumentation Instrumentation对象
     */
    public static void loadServices(String agentMainArg, File execEnvDir, ClassLoader classLoader,
            Instrumentation instrumentation) {
        for (AdaptorService service : ServiceLoader.load(AdaptorService.class, classLoader)) {
            final Class<? extends AdaptorService> serviceClass = service.getClass();
            if (!SERVICES.containsKey(serviceClass) && service.start(agentMainArg, execEnvDir, classLoader,
                    instrumentation)) {
                SERVICES.put(serviceClass, service);
            }
        }
    }

    /**
     * 添加终止钩子
     */
    public static void addStopHook() {
        if (SERVICES.isEmpty()) {
            return;
        }
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                for (AdaptorService adaptorService : SERVICES.values()) {
                    adaptorService.stop();
                }
            }
        }));
    }
}
