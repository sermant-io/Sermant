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

package com.huaweicloud.sermant.core.event.collector;

import com.huaweicloud.sermant.core.config.ConfigManager;
import com.huaweicloud.sermant.core.event.Event;
import com.huaweicloud.sermant.core.event.EventCollector;
import com.huaweicloud.sermant.core.event.EventInfo;
import com.huaweicloud.sermant.core.event.config.EventConfig;

/**
 * 框架事件收集器
 *
 * @author luanwenfei
 * @since 2023-03-04
 */
public class FrameworkEventCollector extends EventCollector {
    private static FrameworkEventCollector frameworkEventCollector;

    private final EventConfig eventConfig = ConfigManager.getConfig(EventConfig.class);

    private FrameworkEventCollector() {
    }

    /**
     * 获取框架事件采集器单例
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
     * 采集服务启动事件
     *
     * @param serviceName 服务名
     */
    public void collectServiceStartEvent(String serviceName) {
        if (!eventConfig.isEnable()) {
            return;
        }
        String eventDescription = "Start service: " + serviceName + ".";
        offerEvent(new Event(FrameworkEventDefinitions.SERMANT_SERVICE_START.getScope(),
                FrameworkEventDefinitions.SERMANT_SERVICE_START.getEventLevel(),
                FrameworkEventDefinitions.SERMANT_SERVICE_START.getEventType(),
                new EventInfo(FrameworkEventDefinitions.SERMANT_SERVICE_START.getName(), eventDescription)));
    }

    /**
     * 采集服务关闭事件
     *
     * @param serviceName 服务名
     */
    public void collectServiceStopEvent(String serviceName) {
        if (!eventConfig.isEnable()) {
            return;
        }
        String eventDescription = "Stop service: " + serviceName + ".";
        offerEvent(new Event(FrameworkEventDefinitions.SERMANT_SERVICE_STOP.getScope(),
                FrameworkEventDefinitions.SERMANT_SERVICE_STOP.getEventLevel(),
                FrameworkEventDefinitions.SERMANT_SERVICE_STOP.getEventType(),
                new EventInfo(FrameworkEventDefinitions.SERMANT_SERVICE_STOP.getName(), eventDescription)));
    }

    /**
     * 采集插件加载事件
     *
     * @param plugin 插件名
     */
    public void collectPluginsLoadEvent(String plugin) {
        if (!eventConfig.isEnable()) {
            return;
        }
        String eventDescription = "Load plugin: " + plugin + " successful.";
        offerEvent(new Event(FrameworkEventDefinitions.SERMANT_PLUGIN_LOAD.getScope(),
                FrameworkEventDefinitions.SERMANT_PLUGIN_LOAD.getEventLevel(),
                FrameworkEventDefinitions.SERMANT_PLUGIN_LOAD.getEventType(),
                new EventInfo(FrameworkEventDefinitions.SERMANT_PLUGIN_LOAD.getName(), eventDescription)));
    }

    /**
     * 采集字节码增强成功事件
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
     * 采集字节码增强失败事件
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
}
