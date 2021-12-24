/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 * Based on org/apache/skywalking/apm/agent/core/boot/ServiceManager.java
 * from the Apache Skywalking project.
 */

package com.huawei.sermant.core.service;

import com.huawei.sermant.core.exception.DupServiceException;
import com.huawei.sermant.core.util.SpiLoadUtil;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.ServiceLoader;

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
                        throw new DupServiceException(serviceName);
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
