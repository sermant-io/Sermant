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

package io.sermant.removal.cache;

import io.sermant.removal.common.RemovalConstants;
import io.sermant.removal.entity.InstanceInfo;
import io.sermant.removal.entity.RequestInfo;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service instance cache class
 *
 * @author zhp
 * @since 2023-02-17
 */
public class InstanceCache {
    /**
     * Instance information caches MAP
     */
    public static final Map<String, InstanceInfo> INSTANCE_MAP = new ConcurrentHashMap<>();

    private InstanceCache() {
    }

    /**
     * Invocation information is saved
     *
     * @param requestInfo Service call information
     */
    public static void saveInstanceInfo(RequestInfo requestInfo) {
        String key = requestInfo.getHost() + RemovalConstants.CONNECTOR + requestInfo.getPort();
        InstanceInfo info = INSTANCE_MAP.computeIfAbsent(key, value -> {
            InstanceInfo instanceInfo = new InstanceInfo();
            instanceInfo.setHost(requestInfo.getHost());
            instanceInfo.setPort(requestInfo.getPort());
            return instanceInfo;
        });
        if (!requestInfo.isSuccess()) {
            info.getRequestFailNum().getAndIncrement();
        }
        info.getRequestNum().getAndIncrement();
        info.setLastInvokeTime(requestInfo.getRequestTime());
    }
}
