/*
 * Copyright (C) 2023-2023 Huawei Technologies Co., Ltd. All rights reserved.
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

package io.sermant.discovery.event;

import io.sermant.core.config.ConfigManager;
import io.sermant.core.event.Event;
import io.sermant.core.event.EventCollector;
import io.sermant.core.event.EventInfo;
import io.sermant.core.event.EventManager;
import io.sermant.core.event.config.EventConfig;
import io.sermant.discovery.entity.DefaultServiceInstance;

/**
 * Springboot registers the plugin event collector
 *
 * @author lilai
 * @since 2023-04-14
 */
public class SpringBootRegistryEventCollector extends EventCollector {
    private static volatile SpringBootRegistryEventCollector collector;

    private final EventConfig eventConfig = ConfigManager.getConfig(EventConfig.class);

    private SpringBootRegistryEventCollector() {
    }

    /**
     * Obtain the SpringBoot Registered plugin Event Collector singleton
     *
     * @return Springboot registers the plugin event collector singleton
     */
    public static SpringBootRegistryEventCollector getInstance() {
        if (collector == null) {
            synchronized (SpringBootRegistryEventCollector.class) {
                if (collector == null) {
                    collector = new SpringBootRegistryEventCollector();
                    EventManager.registerCollector(SpringBootRegistryEventCollector.getInstance());
                }
            }
        }
        return collector;
    }

    /**
     * Collect service registration events
     *
     * @param instance Microservice instances
     */
    public void collectRegistryEvent(DefaultServiceInstance instance) {
        if (!eventConfig.isEnable()) {
            return;
        }
        String eventDescription = "Service instance register:" + instance.toString();
        offerEvent(new Event(SpringBootRegistryEventDefinition.SPRINGBOOT_REGISTRY.getScope(),
                SpringBootRegistryEventDefinition.SPRINGBOOT_REGISTRY.getEventLevel(),
                SpringBootRegistryEventDefinition.SPRINGBOOT_REGISTRY.getEventType(),
                new EventInfo(SpringBootRegistryEventDefinition.SPRINGBOOT_REGISTRY.getName(), eventDescription)));
    }

    /**
     * Capture service removes registration events
     *
     * @param instance Microservice instances
     */
    public void collectUnRegistryEvent(DefaultServiceInstance instance) {
        if (!eventConfig.isEnable()) {
            return;
        }
        String eventDescription = "Service instance unregister:" + instance.toString();
        offerEvent(new Event(SpringBootRegistryEventDefinition.SPRINGBOOT_UNREGISTRY.getScope(),
                SpringBootRegistryEventDefinition.SPRINGBOOT_UNREGISTRY.getEventLevel(),
                SpringBootRegistryEventDefinition.SPRINGBOOT_UNREGISTRY.getEventType(),
                new EventInfo(SpringBootRegistryEventDefinition.SPRINGBOOT_UNREGISTRY.getName(), eventDescription)));
    }

    /**
     * Collect grayscale configuration refresh events
     *
     * @param config Grayscale configuration
     */
    public void collectGrayConfigRefreshEvent(String config) {
        if (!eventConfig.isEnable()) {
            return;
        }
        String eventDescription = "Gray config refresh:" + config;
        offerEvent(new Event(SpringBootRegistryEventDefinition.SPRINGBOOT_GRAY_CONFIG_REFRESH.getScope(),
                SpringBootRegistryEventDefinition.SPRINGBOOT_GRAY_CONFIG_REFRESH.getEventLevel(),
                SpringBootRegistryEventDefinition.SPRINGBOOT_GRAY_CONFIG_REFRESH.getEventType(),
                new EventInfo(SpringBootRegistryEventDefinition.SPRINGBOOT_GRAY_CONFIG_REFRESH.getName(),
                        eventDescription)));
    }
}
