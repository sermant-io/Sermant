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

package com.huaweicloud.sermant.service;

import com.huaweicloud.sermant.config.RemovalConfig;
import com.huaweicloud.sermant.core.event.Event;
import com.huaweicloud.sermant.core.event.EventInfo;
import com.huaweicloud.sermant.core.event.EventManager;
import com.huaweicloud.sermant.core.plugin.config.PluginConfigManager;
import com.huaweicloud.sermant.entity.InstanceInfo;
import com.huaweicloud.sermant.event.RemovalEventCollector;
import com.huaweicloud.sermant.event.RemovalEventDefinitions;

/**
 * 离群事件采集服务
 *
 * @author zhp
 * @since 2023-02-27
 */
public class RemovalEventServiceImpl implements RemovalEventService {
    private static final String CONNECTOR = ":";

    private final RemovalEventCollector removalEventCollector = new RemovalEventCollector();

    private final RemovalConfig removalConfig = PluginConfigManager.getConfig(RemovalConfig.class);

    @Override
    public void start() {
        if (removalConfig.isEnableRemoval()) {
            EventManager.registerCollector(removalEventCollector);
        }
    }

    @Override
    public void stop() {
        if (removalConfig.isEnableRemoval()) {
            EventManager.unRegisterCollector(removalEventCollector);
        }
    }

    @Override
    public void reportRemovalEvent(InstanceInfo info) {
        removalEventCollector.offerEvent(getEvent(RemovalEventDefinitions.INSTANCE_REMOVAL, info));
    }

    /**
     * 创建事件信息
     *
     * @param eventDefinition 事件定义
     * @param info 实例信息
     * @return 事件信息
     */
    private static Event getEvent(RemovalEventDefinitions eventDefinition, InstanceInfo info) {
        String description = eventDefinition.getDescription() + info.getHost() + CONNECTOR + info.getPort();
        EventInfo eventInfo = new EventInfo(eventDefinition.getName(), description);
        return new Event(eventDefinition.getScope(), eventDefinition.getEventLevel(), eventDefinition.getEventType(),
                eventInfo);
    }

    @Override
    public void reportRecoveryEvent(InstanceInfo info) {
        removalEventCollector.offerEvent(getEvent(RemovalEventDefinitions.INSTANCE_RECOVERY, info));
    }
}
