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

package com.huaweicloud.sermant.core.service;

import com.huaweicloud.sermant.core.classloader.ClassLoaderManager;
import com.huaweicloud.sermant.core.common.LoggerFactory;
import com.huaweicloud.sermant.core.config.ConfigManager;
import com.huaweicloud.sermant.core.event.EventManager;
import com.huaweicloud.sermant.core.event.collector.FrameworkEventCollector;
import com.huaweicloud.sermant.core.exception.DupServiceException;
import com.huaweicloud.sermant.core.plugin.agent.config.AgentConfig;
import com.huaweicloud.sermant.core.utils.SpiLoadUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 服务管理器
 *
 * @author justforstudy-A, beetle-man, HapThorin
 * @version 1.0.0
 * @since 2021-10-26
 */
public class ServiceManager {
    /**
     * 日志
     */
    private static final Logger LOGGER = LoggerFactory.getLogger();

    /**
     * 服务集合
     */
    private static final Map<String, BaseService> SERVICES = new HashMap<String, BaseService>();

    /**
     * agent配置
     */
    private static final AgentConfig AGENT_CONFIG = ConfigManager.getConfig(AgentConfig.class);

    /**
     * Constructor.
     */
    protected ServiceManager() {
    }

    /**
     * 初始化所有服务
     */
    public static void initServices() {
        ArrayList<String> startServiceArray = new ArrayList<>();
        for (final BaseService service : ServiceLoader.load(BaseService.class,
                ClassLoaderManager.getFrameworkClassLoader())) {
            if (!AGENT_CONFIG.getServiceBlackList().contains(service.getClass().getName())
                    && loadService(service, service.getClass(), BaseService.class)) {
                service.start();
                startServiceArray.add(service.getClass().getName());
            }
        }
        FrameworkEventCollector.getInstance().collectServiceStartEvent(startServiceArray.toString());
        addStopHook(); // 加载完所有服务再启动服务
    }

    /**
     * 获取服务对象
     *
     * @param serviceClass 服务class
     * @param <T> 服务泛型
     * @return 服务实例对象
     */
    public static <T extends BaseService> T getService(Class<T> serviceClass) {
        final BaseService baseService = SERVICES.get(serviceClass.getName());
        if (baseService != null && serviceClass.isAssignableFrom(baseService.getClass())) {
            return (T) baseService;
        }
        throw new IllegalArgumentException("Service instance of [" + serviceClass + "] is not found. ");
    }

    /**
     * 加载服务对象至服务集中
     *
     * @param service 服务对象
     * @param serviceCls 服务class
     * @param baseCls 服务基class，用于spi
     * @return 是否加载成功
     */
    protected static boolean loadService(BaseService service, Class<?> serviceCls,
            Class<? extends BaseService> baseCls) {
        if (serviceCls == null || serviceCls == baseCls || !baseCls.isAssignableFrom(serviceCls)) {
            return false;
        }
        final String serviceName = serviceCls.getName();
        final BaseService oldService = SERVICES.get(serviceName);
        if (oldService != null && oldService.getClass() == service.getClass()) {
            return false;
        }
        boolean isLoadSucceed = false;
        final BaseService betterService =
                SpiLoadUtils.getBetter(oldService, service, new SpiLoadUtils.WeightEqualHandler<BaseService>() {
                    @Override
                    public BaseService handle(BaseService source, BaseService target) {
                        throw new DupServiceException(serviceName);
                    }
                });
        if (betterService != oldService) {
            SERVICES.put(serviceName, service);
            isLoadSucceed = true;
        }
        isLoadSucceed |= loadService(service, serviceCls.getSuperclass(), baseCls);
        for (Class<?> interfaceCls : serviceCls.getInterfaces()) {
            isLoadSucceed |= loadService(service, interfaceCls, baseCls);
        }
        return isLoadSucceed;
    }

    /**
     * 添加关闭服务的钩子
     */
    private static void addStopHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                offerEvent();
                for (BaseService baseService : new HashSet<>(SERVICES.values())) {
                    try {
                        baseService.stop();
                    } catch (Exception ex) {
                        LOGGER.log(Level.SEVERE, String.format(Locale.ENGLISH,
                                "Error occurs while stopping service: %s", baseService.getClass().toString()), ex);
                    }
                }
            }
        }));
    }

    private static void offerEvent() {
        // 上报服务关闭事件
        for (BaseService baseService : new HashSet<>(SERVICES.values())) {
            FrameworkEventCollector.getInstance().collectServiceStopEvent(baseService.getClass().getName());
        }

        // 上报Sermant关闭的事件
        FrameworkEventCollector.getInstance().collectAgentStopEvent();
        EventManager.shutdown();
    }
}
