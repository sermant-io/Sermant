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

package com.huaweicloud.sermant.router.common.event;

import com.huaweicloud.sermant.core.config.ConfigManager;
import com.huaweicloud.sermant.core.event.Event;
import com.huaweicloud.sermant.core.event.EventCollector;
import com.huaweicloud.sermant.core.event.EventInfo;
import com.huaweicloud.sermant.core.event.EventManager;
import com.huaweicloud.sermant.core.event.config.EventConfig;

/**
 * Routing plug-in event collector
 *
 * @author lilai
 * @since 2023-03-28
 */
public class RouterEventCollector extends EventCollector {
    private static volatile RouterEventCollector routerEventCollector;

    private final EventConfig eventConfig = ConfigManager.getConfig(EventConfig.class);

    private RouterEventCollector() {
    }

    /**
     * Obtain the singleton of the event collector of the routing plug-in
     *
     * @return Routing plug-in event collector singleton
     */
    public static RouterEventCollector getInstance() {
        if (routerEventCollector == null) {
            synchronized (RouterEventCollector.class) {
                if (routerEventCollector == null) {
                    routerEventCollector = new RouterEventCollector();
                    EventManager.registerCollector(RouterEventCollector.getInstance());
                }
            }
        }
        return routerEventCollector;
    }

    /**
     * Collect the events when the service granularity rule takes effect
     *
     * @param rule Rules for routing plug-ins
     */
    public void collectServiceRouteRuleEvent(String rule) {
        if (!eventConfig.isEnable()) {
            return;
        }
        String eventDescription = "Service router rule refresh:" + System.lineSeparator() + rule;
        offerEvent(new Event(RouterEventDefinition.ROUTER_RULE_REFRESH.getScope(),
                RouterEventDefinition.ROUTER_RULE_REFRESH.getEventLevel(),
                RouterEventDefinition.ROUTER_RULE_REFRESH.getEventType(),
                new EventInfo(RouterEventDefinition.ROUTER_RULE_REFRESH.getName(), eventDescription)));
    }

    /**
     * Collect events for global granularity rules to take effect
     *
     * @param rule Rules for routing plug-ins
     */
    public void collectGlobalRouteRuleEvent(String rule) {
        if (!eventConfig.isEnable()) {
            return;
        }
        String eventDescription = "Global router rule refresh:" + System.lineSeparator() + rule;
        offerEvent(new Event(RouterEventDefinition.ROUTER_RULE_REFRESH.getScope(),
                RouterEventDefinition.ROUTER_RULE_REFRESH.getEventLevel(),
                RouterEventDefinition.ROUTER_RULE_REFRESH.getEventType(),
                new EventInfo(RouterEventDefinition.ROUTER_RULE_REFRESH.getName(), eventDescription)));
    }

    /**
     * Collect and select the downstream service name to match valid events
     *
     * @param tagMsg tags message
     * @param serviceName service name
     * @param reason cause
     */
    public void collectSameTagMatchedEvent(String tagMsg, String serviceName, String reason) {
        if (!eventConfig.isEnable()) {
            return;
        }
        String eventDescription = "The matching with the tag (" + tagMsg + ") rule takes effect when request "
                + "service is " + serviceName + " (reason: " + reason + ")";
        offerEvent(new Event(RouterEventDefinition.SAME_TAG_RULE_MATCH.getScope(),
                RouterEventDefinition.SAME_TAG_RULE_MATCH.getEventLevel(),
                RouterEventDefinition.SAME_TAG_RULE_MATCH.getEventType(),
                new EventInfo(RouterEventDefinition.SAME_TAG_RULE_MATCH.getName(), eventDescription)));
    }

    /**
     * Collect and select invalid events for downstream service name matching
     *
     * @param tagMsg tags message
     * @param serviceName service name
     * @param reason cause
     */
    public void collectSameTagMisMatchedEvent(String tagMsg, String serviceName, String reason) {
        if (!eventConfig.isEnable()) {
            return;
        }
        String eventDescription = "The matching with the tag (" + tagMsg + ") rule did not take effect when request "
                + "service is " + serviceName + " (reason: " + reason + ")";
        offerEvent(new Event(RouterEventDefinition.SAME_TAG_RULE_MISMATCH.getScope(),
                RouterEventDefinition.SAME_TAG_RULE_MISMATCH.getEventLevel(),
                RouterEventDefinition.SAME_TAG_RULE_MISMATCH.getEventType(),
                new EventInfo(RouterEventDefinition.SAME_TAG_RULE_MISMATCH.getName(), eventDescription)));
    }
}
