/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.apm.bootstrap.boot;

import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;

/**
 * <Code>CoreService<Code/>管理器，加载、启动所有定义的服务实例。
 *
 * <p><Code>CoreService<Code/>定义在SPI声明文件当中。</p>
 */
public enum CoreServiceManager {

    INSTANCE;

    private final Map<String, CoreService> services = new HashMap<String, CoreService>();

    public void initServices() {
        for (final CoreService service : ServiceLoader.load(CoreService.class)) {
            loadService(service);
        }
        // 加载完所有服务再启动服务
        startService();
        addStopHook();
    }

    public <T> T getService(Class<T> serviceClass) {
        return (T) services.get(serviceClass.getName());
    }

    private void loadService(CoreService service) {
        Class<? extends CoreService> serviceClass = service.getClass();
        Class<?>[] serviceInterfaces = serviceClass.getInterfaces();
        for (Class<?> interfaceClass : serviceInterfaces) {
            if (interfaceClass.equals(CoreService.class)) {
                continue;
            }
            String serviceName = interfaceClass.getName();
            CoreService existingService = services.get(serviceName);
            if (existingService == null) {
                services.put(serviceName, service);
            } else {
                if (existingService.getClass().isAnnotationPresent(Replacement.class)) {
                    throw new ServiceInitException("More than one replacement of service" + serviceName
                        + "was found.");
                } else if (serviceClass.isAnnotationPresent(Replacement.class)) {
                    // Replace default implementor.
                    services.put(serviceName, service);
                } else {
                    throw new ServiceInitException("More than one default implementor of service" + serviceName
                        + "was found.");
                }
            }
        }
    }

    private void startService() {
        for (CoreService service : services.values()) {
            service.start();
        }
    }

    private void addStopHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                for (CoreService service : services.values()) {
                    service.stop();
                }
            }
        }));
    }
}
