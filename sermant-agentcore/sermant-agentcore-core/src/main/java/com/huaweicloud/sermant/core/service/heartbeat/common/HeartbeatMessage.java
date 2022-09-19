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

package com.huaweicloud.sermant.core.service.heartbeat.common;

import com.huaweicloud.sermant.core.common.BootArgsIndexer;
import com.huaweicloud.sermant.core.utils.NetworkUtils;
import com.huaweicloud.sermant.core.utils.TimeUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * 心跳消息
 *
 * @author luanwenfei
 * @since 2022-03-19
 */
public class HeartbeatMessage {
    private final Map<String, Object> message = new HashMap<>();

    /**
     * 构造函数
     */
    public HeartbeatMessage() {
        message.put("hostname", NetworkUtils.getHostName());
        message.put("ip", NetworkUtils.getAllNetworkIp());
        message.put("app", BootArgsIndexer.getAppName());
        message.put("appType", BootArgsIndexer.getAppType());
        message.put("heartbeatVersion", String.valueOf(TimeUtils.currentTimeMillis()));
        message.put("lastHeartbeat", String.valueOf(TimeUtils.currentTimeMillis()));
        message.put("version", BootArgsIndexer.getCoreVersion());
        message.put("instanceId", BootArgsIndexer.getInstanceId());
    }

    /**
     * registerInformation
     *
     * @param key key
     * @param value value
     * @return HeartbeatMessage
     */
    public HeartbeatMessage registerInformation(String key, String value) {
        message.put(key, value);
        return this;
    }

    public Map<String, Object> getMessage() {
        return message;
    }
}
