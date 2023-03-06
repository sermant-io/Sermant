/*
 * Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
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

import com.huaweicloud.sermant.backend.entity.HeartbeatEntity;
import com.huaweicloud.sermant.backend.entity.ServerInfo;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 心跳数据缓存
 *
 * @author xuezechao
 * @since 2022-02-15
 */
public class HeartbeatCache {
    private static Map<String, HeartbeatEntity> heartbeatMessages = new ConcurrentHashMap<>();

    private static Map<String, ServerInfo> lastHeartBeatDate = new ConcurrentHashMap<>();

    private HeartbeatCache() {
    }

    public static Map<String, HeartbeatEntity> getHeartbeatMessages() {
        return heartbeatMessages;
    }

    public static Map<String, ServerInfo> getHeartbeatDate() {
        return lastHeartBeatDate;
    }
}
