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
 * 路由插件事件采集器
 *
 * @author lilai
 * @since 2023-03-28
 */
public class RouterEventCollector extends EventCollector {
    private static volatile RouterEventCollector routerEventCollecter;

    private final EventConfig eventConfig = ConfigManager.getConfig(EventConfig.class);

    private RouterEventCollector() {
    }

    /**
     * 获取路由插件事件采集器单例
     *
     * @return 路由插件事件采集器单例
     */
    public static RouterEventCollector getInstance() {
        if (routerEventCollecter == null) {
            synchronized (RouterEventCollector.class) {
                if (routerEventCollecter == null) {
                    routerEventCollecter = new RouterEventCollector();
                    EventManager.registerCollector(RouterEventCollector.getInstance());
                }
            }
        }
        return routerEventCollecter;
    }

    /**
     * 采集服务粒度规则生效事件
     *
     * @param rule 路由插件的规则
     */
    public void collectServiceRouteRuleEvent(String rule) {
        if (!eventConfig.isEnable()) {
            return;
        }
        String eventDescription = "Service router rule refresh:" + System.lineSeparator() + rule;
        offerEvent(new Event(RouterEventDefinition.ROUTER_RULE_TAKE_EFFECT.getScope(),
                RouterEventDefinition.ROUTER_RULE_TAKE_EFFECT.getEventLevel(),
                RouterEventDefinition.ROUTER_RULE_TAKE_EFFECT.getEventType(),
                new EventInfo(RouterEventDefinition.ROUTER_RULE_TAKE_EFFECT.getName(), eventDescription)));
    }

    /**
     * 采集全局粒度规则生效事件
     *
     * @param rule 路由插件的规则
     */
    public void collectGlobalRouteRuleEvent(String rule) {
        if (!eventConfig.isEnable()) {
            return;
        }
        String eventDescription = "Global router rule refresh:" + System.lineSeparator() + rule;
        offerEvent(new Event(RouterEventDefinition.ROUTER_RULE_TAKE_EFFECT.getScope(),
                RouterEventDefinition.ROUTER_RULE_TAKE_EFFECT.getEventLevel(),
                RouterEventDefinition.ROUTER_RULE_TAKE_EFFECT.getEventType(),
                new EventInfo(RouterEventDefinition.ROUTER_RULE_TAKE_EFFECT.getName(), eventDescription)));
    }
}
