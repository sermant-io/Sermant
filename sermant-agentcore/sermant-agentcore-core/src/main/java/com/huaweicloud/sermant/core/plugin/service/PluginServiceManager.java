/*
 * Copyright (C) 2021-2024 Sermant Authors. All rights reserved.
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

package com.huaweicloud.sermant.core.plugin.service;

import com.huaweicloud.sermant.core.common.LoggerFactory;
import com.huaweicloud.sermant.core.event.collector.FrameworkEventCollector;
import com.huaweicloud.sermant.core.plugin.Plugin;
import com.huaweicloud.sermant.core.service.ServiceManager;
import com.huaweicloud.sermant.core.utils.KeyGenerateUtils;

import java.util.ServiceLoader;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Plugin service manager, a specialization of the core service manager {@link ServiceManager} to initialize {@link
 * PluginService}
 *
 * @author HapThorin
 * @version 1.0.0
 * @since 2021-11-12
 */
public class PluginServiceManager extends ServiceManager {
    private static final Logger LOGGER = LoggerFactory.getLogger();

    private PluginServiceManager() {
        super();
    }

    /**
     * Initialize plugin services
     *
     * @param plugin plugin
     */
    public static void initPluginServices(Plugin plugin) {
        ClassLoader classLoader =
                plugin.getServiceClassLoader() != null ? plugin.getServiceClassLoader() : plugin.getPluginClassLoader();
        for (PluginService service : ServiceLoader.load(PluginService.class, classLoader)) {
            if (loadService(service, service.getClass(), PluginService.class)) {
                try {
                    String pluginServiceKey = KeyGenerateUtils.generateClassKeyWithClassLoader(service.getClass());
                    service.start();
                    plugin.getServices().add(pluginServiceKey);
                } catch (Exception ex) {
                    LOGGER.log(Level.SEVERE, "Error occurs while starting plugin service: " + service.getClass(), ex);
                }
            }
        }
        FrameworkEventCollector.getInstance().collectServiceStartEvent(plugin.getServices().toString());
    }

    /**
     * Stop plugin services
     *
     * @param plugin plugin
     */
    public static void shutdownPluginServices(Plugin plugin) {
        for (String serviceName : plugin.getServices()) {
            stopService(serviceName);
        }
    }

    /**
     * Get plugin service
     *
     * @param serviceClass Plugin service class
     * @param <T> Plugin service type
     * @return Plugin service instance
     */
    public static <T extends PluginService> T getPluginService(Class<T> serviceClass) {
        return getService(serviceClass);
    }
}
