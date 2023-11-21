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

package com.huaweicloud.sermant.cache;

import com.huaweicloud.sermant.common.RemovalConstants;
import com.huaweicloud.sermant.entity.InstanceInfo;
import com.huaweicloud.sermant.entity.RequestInfo;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 服务实例缓存类
 *
 * @author zhp
 * @since 2023-02-17
 */
public class InstanceCache {
    /**
     * 实例信息缓存MAP
     */
    public static final Map<String, InstanceInfo> INSTANCE_MAP = new ConcurrentHashMap<>();

    private InstanceCache() {
    }

    /**
     * 调用信息保存
     *
     * @param requestInfo 服务调用信息
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
