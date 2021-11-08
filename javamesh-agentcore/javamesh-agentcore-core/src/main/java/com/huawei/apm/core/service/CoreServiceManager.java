/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.apm.core.service;

import com.huawei.apm.core.classloader.PluginClassLoader;
import com.huawei.apm.core.exception.ServiceInitException;
import com.huawei.apm.core.lubanops.bootstrap.log.LogFactory;
import com.huawei.apm.core.util.SpiLoadUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * <Code>CoreService<Code/>管理器，加载、启动所有定义的服务实例。
 *
 * <p><Code>CoreService<Code/>定义在SPI声明文件当中。</p>
 */
public enum CoreServiceManager {

    INSTANCE;

    private static final Logger LOGGER = LogFactory.getLogger();

    private final Map<String, CoreService> services = new HashMap<String, CoreService>();

    public void initServices() {
        for (final CoreService service : PluginClassLoader.load(CoreService.class)) {
            loadService(service);
        }
        // 加载完所有服务再启动服务
        startService();
        addStopHook();
    }

    public <T> T getService(Class<T> serviceClass) {
        return (T) services.get(serviceClass.getName());
    }

    public <T> T getService(String className) {
        return (T) services.get(className);
    }

    private void loadService(CoreService service) {
        Class<? extends CoreService> serviceClass = service.getClass();
        Class<?>[] serviceInterfaces = serviceClass.getInterfaces();
        for (Class<?> interfaceClass : serviceInterfaces) {
            if (interfaceClass.equals(CoreService.class)) {
                continue;
            }
            String serviceName = interfaceClass.getName();
            if (!SpiLoadUtil.compare(services.get(serviceName), service)) {
                services.put(serviceName, service);
            }
        }
    }

    private void startService() {
        for (Map.Entry<String, CoreService> serviceEntry : services.entrySet()) {
            try {
                serviceEntry.getValue().start();
            } catch (Exception e) {
                throw new ServiceInitException("Failed to start service [" + serviceEntry.getKey() + "] cause by: ", e);
            }
        }
    }

    private void addStopHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                for (Map.Entry<String, CoreService> serviceEntry : services.entrySet()) {
                    try {
                        serviceEntry.getValue().stop();
                    } catch (Exception e) {
                        LOGGER.warning("Failed to stop service [" + serviceEntry.getKey() + "] since: "
                                + e.getMessage());
                    }
                }
            }
        }));
    }
}
