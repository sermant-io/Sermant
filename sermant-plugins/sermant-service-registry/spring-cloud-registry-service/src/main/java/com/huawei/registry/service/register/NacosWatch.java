/*
 * Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huawei.registry.service.register;

import com.huawei.registry.config.NacosRegisterConfig;
import com.huawei.registry.context.RegisterContext;
import com.huawei.registry.service.config.HeartbeatEvent;

import com.huaweicloud.sermant.core.common.LoggerFactory;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.listener.Event;
import com.alibaba.nacos.api.naming.listener.EventListener;
import com.alibaba.nacos.api.naming.listener.NamingEvent;
import com.alibaba.nacos.api.naming.pojo.Instance;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.context.SmartLifecycle;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * nacos服务实例监听
 *
 * @author chengyouling
 * @since 2022-10-24
 */
public class NacosWatch implements ApplicationEventPublisherAware, SmartLifecycle, DisposableBean {

    private static final Logger LOGGER = LoggerFactory.getLogger();

    private Map<String, EventListener> listenerMap = new ConcurrentHashMap<>(16);

    private final AtomicBoolean running = new AtomicBoolean(false);

    private final AtomicLong nacosWatchIndex = new AtomicLong(0);

    private ApplicationEventPublisher publisher;

    private ScheduledFuture<?> watchFuture;

    private final NacosServiceManager nacosServiceManager;

    private final ThreadPoolTaskScheduler taskScheduler;

    private final NacosRegisterConfig config;

    private final String currentServiceId;

    /**
     * 构造方法
     *
     * @param nacosServiceManager
     * @param config
     * @param serviceId
     */
    public NacosWatch(NacosServiceManager nacosServiceManager, NacosRegisterConfig config, String serviceId) {
        this.nacosServiceManager = nacosServiceManager;
        this.config = config;
        this.currentServiceId = serviceId;
        this.taskScheduler = getTaskScheduler();
    }

    private static ThreadPoolTaskScheduler getTaskScheduler() {
        ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
        taskScheduler.setBeanName("nacos-watch-task-scheduler");
        taskScheduler.initialize();
        return taskScheduler;
    }

    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.publisher = applicationEventPublisher;
    }

    @Override
    public boolean isAutoStartup() {
        return true;
    }

    @Override
    public void stop(Runnable callback) {
        this.stop();
        callback.run();
    }

    @Override
    public void stop() {
        if (this.running.compareAndSet(true, false)) {
            if (this.watchFuture != null) {
                this.taskScheduler.shutdown();
                this.watchFuture.cancel(true);
            }

            EventListener eventListener = listenerMap.get(buildKey());
            try {
                NamingService namingService = nacosServiceManager.getNamingService();
                namingService.unsubscribe(currentServiceId, config.getGroup(),
                        Arrays.asList(config.getClusterName()), eventListener);
            } catch (NacosException e) {
                LOGGER.log(Level.SEVERE, String.format(Locale.ENGLISH, "namingService unsubscribe failed, "
                        + "serviceId：", currentServiceId), e);
            }
        }
    }

    @Override
    public void start() {
        if (this.running.compareAndSet(false, true)) {
            EventListener eventListener = listenerMap.computeIfAbsent(buildKey(),
                    event -> new EventListener() {
                        @Override
                        public void onEvent(Event event) {
                            if (event instanceof NamingEvent) {
                                List<Instance> instances = ((NamingEvent) event)
                                        .getInstances();
                                Optional<Instance> instanceOptional = selectCurrentInstance(
                                        instances);
                                instanceOptional.ifPresent(currentInstance -> {
                                    resetIfNeeded(currentInstance);
                                });
                            }
                        }
                    });

            NamingService namingService = nacosServiceManager.getNamingService();
            try {
                namingService.subscribe(currentServiceId, config.getGroup(),
                        Arrays.asList(config.getClusterName()), eventListener);
            } catch (NacosException e) {
                LOGGER.log(Level.SEVERE, String.format(Locale.ENGLISH, "namingService subscribe failed, "
                        + "serviceId：", currentServiceId), e);
            }

            this.watchFuture = this.taskScheduler.scheduleWithFixedDelay(
                    this::nacosServicesWatch, this.config.getWatchDelay());
        }
    }

    @Override
    public boolean isRunning() {
        return this.running.get();
    }

    @Override
    public int getPhase() {
        return 0;
    }

    @Override
    public void destroy() {
        this.stop();
    }

    private String buildKey() {
        return String.join(":", currentServiceId, config.getGroup());
    }

    private void resetIfNeeded(Instance instance) {
        if (!RegisterContext.INSTANCE.getClientInfo().getMeta().equals(instance.getMetadata())) {
            RegisterContext.INSTANCE.getClientInfo().setMeta(instance.getMetadata());
        }
    }

    private Optional<Instance> selectCurrentInstance(List<Instance> instances) {
        return instances.stream()
                .filter(instance -> RegisterContext.INSTANCE.getClientInfo().getIp().equals(instance.getIp())
                        && RegisterContext.INSTANCE.getClientInfo().getPort() == instance.getPort())
                .findFirst();
    }

    /**
     * 发布心跳事件
     */
    public void nacosServicesWatch() {
        this.publisher.publishEvent(new HeartbeatEvent(this, nacosWatchIndex.getAndIncrement()));

    }
}
