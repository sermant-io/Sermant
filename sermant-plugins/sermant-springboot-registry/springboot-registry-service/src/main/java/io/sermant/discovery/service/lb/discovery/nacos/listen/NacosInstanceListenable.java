/*
 * Copyright (C) 2024-2024 Sermant Authors. All rights reserved.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package io.sermant.discovery.service.lb.discovery.nacos.listen;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.listener.EventListener;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.alibaba.nacos.client.naming.event.InstancesChangeEvent;
import com.alibaba.nacos.common.notify.Event;
import com.alibaba.nacos.common.notify.NotifyCenter;
import com.alibaba.nacos.common.notify.listener.Subscriber;

import io.sermant.core.common.LoggerFactory;
import io.sermant.core.utils.CollectionUtils;
import io.sermant.discovery.config.RegisterType;
import io.sermant.discovery.entity.ServiceInstance;
import io.sermant.discovery.service.lb.discovery.InstanceChangeListener;
import io.sermant.discovery.service.lb.discovery.InstanceListenable;
import io.sermant.discovery.service.lb.discovery.nacos.NacosServiceManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * nacos Listen
 *
 * @author xz
 * @since 2024-11-12
 */
public class NacosInstanceListenable extends Subscriber<InstancesChangeEvent> implements InstanceListenable {
    private static final Logger LOGGER = LoggerFactory.getLogger();

    private final Map<String, InstanceChangeListener> listenerCache = new ConcurrentHashMap<>();

    private final NacosServiceManager nacosServiceManager = NacosServiceManager.getInstance();

    /**
     * Construct
     */
    public NacosInstanceListenable() {
    }

    @Override
    public void onEvent(InstancesChangeEvent instancesChangeEvent) {
        String serviceName = instancesChangeEvent.getServiceName();
        List<Instance> instances = instancesChangeEvent.getHosts();
        InstanceChangeListener listener = listenerCache.get(serviceName);
        if (Objects.isNull(listener)) {
            return;
        }
        if (CollectionUtils.isEmpty(instances)) {
            listener.notify(serviceName, Collections.emptyList());
            return;
        }
        List<ServiceInstance> serviceInstances = new ArrayList<>(instances.size());
        for (Instance instance : instances) {
            if (instance.isEnabled() && instance.isHealthy()) {
                Optional<ServiceInstance> optional = nacosServiceManager.convertServiceInstance(instance, serviceName);
                optional.ifPresent(serviceInstances::add);
            }
        }
        listener.notify(serviceName, serviceInstances);
    }

    @Override
    public Class<? extends Event> subscribeType() {
        return InstancesChangeEvent.class;
    }

    @Override
    public void init() {
    }

    @Override
    public void tryAdd(String serviceName, InstanceChangeListener listener) {
        NotifyCenter.registerSubscriber(this);
        try {
            NamingService namingService = nacosServiceManager.getNamingService();
            namingService.subscribe(serviceName, new EventListener() {
                @Override
                public void onEvent(com.alibaba.nacos.api.naming.listener.Event event) {
                    LOGGER.info("Receive nacos instance change event: %s");
                }
            });
            listenerCache.put(serviceName, listener);
        } catch (NacosException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close() {
        listenerCache.clear();
    }

    @Override
    public RegisterType registerType() {
        return RegisterType.NACOS;
    }
}
