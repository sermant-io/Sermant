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

package io.sermant.core.event.collector;

import io.sermant.core.event.Event;
import io.sermant.core.event.EventCollector;
import io.sermant.core.event.EventInfo;

/**
 * Framework event collector
 *
 * @author luanwenfei
 * @since 2023-03-04
 */
public class FrameworkEventCollector extends EventCollector {
    private static FrameworkEventCollector frameworkEventCollector;

    private FrameworkEventCollector() {
    }

    /**
     * Get the framework event collector singleton instance
     *
     * @return FrameworkEventCollector
     */
    public static synchronized FrameworkEventCollector getInstance() {
        if (frameworkEventCollector == null) {
            frameworkEventCollector = new FrameworkEventCollector();
        }
        return frameworkEventCollector;
    }

    /**
     * Collect agent startup event
     */
    public void collectAgentStartEvent() {
        if (!eventConfig.isEnable()) {
            return;
        }
        String eventDescription = "Start Sermant-Agent done.";
        offerEvent(new Event(FrameworkEventDefinitions.SERMANT_START.getScope(),
                FrameworkEventDefinitions.SERMANT_START.getEventLevel(),
                FrameworkEventDefinitions.SERMANT_START.getEventType(),
                new EventInfo(FrameworkEventDefinitions.SERMANT_START.getName(), eventDescription)));
    }

    /**
     * Collect agent stop event
     */
    public void collectAgentStopEvent() {
        if (!eventConfig.isEnable()) {
            return;
        }
        String eventDescription = "Stop Sermant-Agent.";
        offerEvent(new Event(FrameworkEventDefinitions.SERMANT_STOP.getScope(),
                FrameworkEventDefinitions.SERMANT_STOP.getEventLevel(),
                FrameworkEventDefinitions.SERMANT_STOP.getEventType(),
                new EventInfo(FrameworkEventDefinitions.SERMANT_STOP.getName(), eventDescription)));
    }

    /**
     * Collect service startup event
     *
     * @param serviceName serviceName
     */
    public void collectServiceStartEvent(String serviceName) {
        if (!eventConfig.isEnable()) {
            return;
        }
        String eventDescription = "Start service:" + serviceName + ".";
        offerEvent(new Event(FrameworkEventDefinitions.SERMANT_SERVICE_START.getScope(),
                FrameworkEventDefinitions.SERMANT_SERVICE_START.getEventLevel(),
                FrameworkEventDefinitions.SERMANT_SERVICE_START.getEventType(),
                new EventInfo(FrameworkEventDefinitions.SERMANT_SERVICE_START.getName(), eventDescription)));
    }

    /**
     * Collect service stop event
     *
     * @param serviceName serviceName
     */
    public void collectServiceStopEvent(String serviceName) {
        if (!eventConfig.isEnable()) {
            return;
        }
        String eventDescription = "Stop service: [" + serviceName + "].";
        offerEvent(new Event(FrameworkEventDefinitions.SERMANT_SERVICE_STOP.getScope(),
                FrameworkEventDefinitions.SERMANT_SERVICE_STOP.getEventLevel(),
                FrameworkEventDefinitions.SERMANT_SERVICE_STOP.getEventType(),
                new EventInfo(FrameworkEventDefinitions.SERMANT_SERVICE_STOP.getName(), eventDescription)));
    }

    /**
     * Collect plugin loading event
     *
     * @param plugin plugin name
     */
    public void collectPluginsLoadEvent(String plugin) {
        if (!eventConfig.isEnable()) {
            return;
        }
        String eventDescription = "Load plugin: [" + plugin + "] successful.";
        offerEvent(new Event(FrameworkEventDefinitions.SERMANT_PLUGIN_LOAD.getScope(),
                FrameworkEventDefinitions.SERMANT_PLUGIN_LOAD.getEventLevel(),
                FrameworkEventDefinitions.SERMANT_PLUGIN_LOAD.getEventType(),
                new EventInfo(FrameworkEventDefinitions.SERMANT_PLUGIN_LOAD.getName(), eventDescription)));
    }

    /**
     * Collect bytecode enhancement success event
     *
     * @param transformDescription transformDescription
     */
    public void collectTransformSuccessEvent(String transformDescription) {
        if (!eventConfig.isEnable()) {
            return;
        }
        offerEvent(new Event(FrameworkEventDefinitions.SERMANT_TRANSFORM_SUCCESS.getScope(),
                FrameworkEventDefinitions.SERMANT_TRANSFORM_SUCCESS.getEventLevel(),
                FrameworkEventDefinitions.SERMANT_TRANSFORM_SUCCESS.getEventType(),
                new EventInfo(FrameworkEventDefinitions.SERMANT_TRANSFORM_SUCCESS.getName(), transformDescription)));
    }

    /**
     * Collect bytecode enhancement failure event
     *
     * @param transformDescription transformDescription
     */
    public void collectTransformFailureEvent(String transformDescription) {
        if (!eventConfig.isEnable()) {
            return;
        }
        offerEvent(new Event(FrameworkEventDefinitions.SERMANT_TRANSFORM_FAILURE.getScope(),
                FrameworkEventDefinitions.SERMANT_TRANSFORM_FAILURE.getEventLevel(),
                FrameworkEventDefinitions.SERMANT_TRANSFORM_FAILURE.getEventType(),
                new EventInfo(FrameworkEventDefinitions.SERMANT_TRANSFORM_FAILURE.getName(), transformDescription)));
    }

    /**
     * Collect hot plugging events
     *
     * @param frameworkEventDefinitions Event definition for framework events
     * @param description description
     */
    public void collectdHotPluggingEvent(FrameworkEventDefinitions frameworkEventDefinitions, String description) {
        if (!eventConfig.isEnable()) {
            return;
        }
        offerEvent(new Event(frameworkEventDefinitions.getScope(), frameworkEventDefinitions.getEventLevel(),
                frameworkEventDefinitions.getEventType(),
                new EventInfo(frameworkEventDefinitions.getName(), description)));
    }
}
