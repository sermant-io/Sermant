/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.javamesh.core.service;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.ServiceLoader;

import com.huawei.javamesh.core.exception.DupServiceManager;
import com.huawei.javamesh.core.util.SpiLoadUtil;

/**
 * <Code>CoreService<Code/>管理器，加载、启动所有定义的服务实例。
 *
 * <p><Code>CoreService<Code/>定义在SPI声明文件当中。</p>
 */
public class ServiceManager {
    private static final Map<String, BaseService> services = new HashMap<String, BaseService>();

    public static void initServices() {
        for (final BaseService service : ServiceLoader.load(BaseService.class)) {
            if (loadService(service, service.getClass(), BaseService.class)) {
                service.start();
            }
        }
        // 加载完所有服务再启动服务
        addStopHook();
    }

    public static <T extends BaseService> T getService(Class<T> serviceClass) {
        final BaseService baseService = services.get(serviceClass.getName());
        if (baseService != null && serviceClass.isAssignableFrom(baseService.getClass())) {
            return (T) baseService;
        }
        throw new IllegalArgumentException("Service instance of [" + serviceClass + "] is not found. ");
    }

    protected static boolean loadService(BaseService service, Class<?> serviceCls,
            Class<? extends BaseService> baseCls) {
        if (serviceCls == null || serviceCls == baseCls || !baseCls.isAssignableFrom(serviceCls)) {
            return false;
        }
        final String serviceName = serviceCls.getName();
        final BaseService oldService = services.get(serviceName);
        if (oldService != null && oldService.getClass() == service.getClass()) {
            return false;
        }
        boolean flag = false;
        final BaseService betterService = SpiLoadUtil.getBetter(oldService, service,
                new SpiLoadUtil.WeightEqualHandler<BaseService>() {
                    @Override
                    public BaseService handle(BaseService source, BaseService target) {
                        throw new DupServiceManager(serviceName);
                    }
                });
        if (betterService != oldService) {
            services.put(serviceName, service);
            flag = true;
        }
        flag |= loadService(service, serviceCls.getSuperclass(), baseCls);
        for (Class<?> interfaceCls : serviceCls.getInterfaces()) {
            flag |= loadService(service, interfaceCls, baseCls);
        }
        return flag;
    }

    private static void addStopHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                for (BaseService baseService : new HashSet<>(services.values())) {
                    baseService.stop();
                }
            }
        }));
    }
}
