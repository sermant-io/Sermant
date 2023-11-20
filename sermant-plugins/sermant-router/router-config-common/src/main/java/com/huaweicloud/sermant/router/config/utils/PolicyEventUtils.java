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
 * Policy事件上报工具类
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
     * 通知相同TAG匹配上的事件
     *
     * @param newState 新匹配状态
     * @param tags match里的tags信息
     * @param serviceName 服务名
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
     * 通知相同TAG未匹配上的事件
     *
     * @param newState 新匹配状态
     * @param tags match里的tags信息
     * @param serviceName 服务名
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
