/*
 * Copyright (C) 2022-2024 Sermant Authors. All rights reserved.
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

package com.huaweicloud.sermant.backend.cache;

import com.huaweicloud.sermant.backend.entity.heartbeat.HeartbeatMessage;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Heartbeat data cache
 *
 * @author luanwenfei
 * @since 2022-10-27
 */
public class HeartbeatCache {
    private static final Map<String, HeartbeatMessage> HEARTBEAT_MESSAGE_MAP = new ConcurrentHashMap<>();

    private HeartbeatCache() {
    }

    public static Map<String, HeartbeatMessage> getHeartbeatMessageMap() {
        return HEARTBEAT_MESSAGE_MAP;
    }
}
