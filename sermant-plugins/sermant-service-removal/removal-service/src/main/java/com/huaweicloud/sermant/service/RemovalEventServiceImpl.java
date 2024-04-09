/*
 * Copyright (C) 2023-2024 Sermant Authors. All rights reserved.
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

import com.huaweicloud.sermant.common.RemovalConstants;
import com.huaweicloud.sermant.core.event.Event;
import com.huaweicloud.sermant.core.event.EventInfo;
import com.huaweicloud.sermant.core.event.EventManager;
import com.huaweicloud.sermant.entity.InstanceInfo;
import com.huaweicloud.sermant.event.RemovalEventCollector;
import com.huaweicloud.sermant.event.RemovalEventDefinitions;

/**
 * Outlier event collection service
 *
 * @author zhp
 * @since 2023-02-27
 */
public class RemovalEventServiceImpl implements RemovalEventService {
    private final RemovalEventCollector removalEventCollector = new RemovalEventCollector();

    @Override
    public void start() {
        EventManager.registerCollector(removalEventCollector);
    }

    @Override
    public void stop() {
        EventManager.unRegisterCollector(removalEventCollector);
    }

    @Override
    public void reportRemovalEvent(InstanceInfo info) {
        removalEventCollector.offerEvent(getEvent(RemovalEventDefinitions.INSTANCE_REMOVAL, info));
    }

    /**
     * Create event information
     *
     * @param eventDefinition Event definitions
     * @param info Instance information
     * @return Event information
     */
    private static Event getEvent(RemovalEventDefinitions eventDefinition, InstanceInfo info) {
        String description = eventDefinition.getDescription() + info.getHost() + RemovalConstants.CONNECTOR
                + info.getPort();
        EventInfo eventInfo = new EventInfo(eventDefinition.getName(), description);
        return new Event(eventDefinition.getScope(), eventDefinition.getEventLevel(), eventDefinition.getEventType(),
                eventInfo);
    }

    @Override
    public void reportRecoveryEvent(InstanceInfo info) {
        removalEventCollector.offerEvent(getEvent(RemovalEventDefinitions.INSTANCE_RECOVERY, info));
    }
}
