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

package com.huaweicloud.sermant.router.config.utils;

import com.huaweicloud.sermant.router.common.event.PolicyEvent;
import com.huaweicloud.sermant.router.common.event.RouterEventCollector;
import com.huaweicloud.sermant.router.config.entity.MatchRule;

import com.alibaba.fastjson.JSONObject;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * Policy event reporting tool class
 *
 * @author robotLJW
 * @since 2023-04-06
 */
public class PolicyEventUtils {

    private static final ConcurrentHashMap<String, PolicyEvent> POLICY_EVENT_CONCURRENT_HASH_MAP
            = new ConcurrentHashMap<>();

    private PolicyEventUtils() {
    }

    /**
     * Notify events on the same TAG match
     *
     * @param newState New match status
     * @param tags Tags in match
     * @param serviceName Service name
     */
    public static void notifySameTagMatchedEvent(PolicyEvent newState, Map<String, List<MatchRule>> tags,
            String serviceName) {
        PolicyEvent previousState = POLICY_EVENT_CONCURRENT_HASH_MAP.get(serviceName);
        if (!newState.equals(previousState)) {
            POLICY_EVENT_CONCURRENT_HASH_MAP.put(serviceName, newState);
            RouterEventCollector.getInstance().collectSameTagMatchedEvent(JSONObject.toJSONString(tags), serviceName,
                    newState.getDesc());
        }
    }

    /**
     * Notify the same TAG of the event on which it is not matched
     *
     * @param newState New match status
     * @param tags Tags in match
     * @param serviceName Service name
     */
    public static void notifySameTagMisMatchedEvent(PolicyEvent newState, Map<String, List<MatchRule>> tags,
                                                    String serviceName) {
        PolicyEvent previousState = POLICY_EVENT_CONCURRENT_HASH_MAP.get(serviceName);
        if (!newState.equals(previousState)) {
            POLICY_EVENT_CONCURRENT_HASH_MAP.put(serviceName, newState);
            RouterEventCollector.getInstance().collectSameTagMisMatchedEvent(JSONObject.toJSONString(tags), serviceName,
                    newState.getDesc());
        }
    }

}
