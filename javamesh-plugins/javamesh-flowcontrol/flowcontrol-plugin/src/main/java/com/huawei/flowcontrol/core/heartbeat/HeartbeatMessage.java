/*
 * Copyright (C) 2021-2021 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huawei.flowcontrol.core.heartbeat;

import com.alibaba.csp.sentinel.config.SentinelConfig;
import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.transport.config.TransportConfig;
import com.alibaba.csp.sentinel.util.AppNameUtil;
import com.alibaba.csp.sentinel.util.HostNameUtil;
import com.alibaba.csp.sentinel.util.TimeUtil;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.huawei.flowcontrol.core.config.CommonConst;
import com.huawei.flowcontrol.core.config.ConfigConst;
import com.huawei.flowcontrol.core.util.PluginConfigUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * 心跳数据
 *
 * @author liyi
 * @since 2020-08-26
 */
public enum HeartbeatMessage {
    /**
     * 心跳消息发送
     */
    HEARTBEAT_SEND;

    /**
     * 心跳消息Map
     */
    private final Map<String, Object> message = new HashMap<String, Object>();

    HeartbeatMessage() {
        message.put("hostname", HostNameUtil.getHostName());
        message.put("ip", TransportConfig.getHeartbeatClientIp());
        message.put("app", AppNameUtil.getAppName());
        message.put("appType", String.valueOf(SentinelConfig.getAppType()));
        message.put("port", CommonConst.SENTINEL_PORT);
        message.put("version", PluginConfigUtil.getValueByKey(ConfigConst.SENTINEL_VERSION));
    }

    /**
     * 添加消息
     *
     * @param key   键
     * @param value 值
     * @return 返回消息对象
     */
    public HeartbeatMessage registerInformation(String key, String value) {
        message.put(key, value);
        return this;
    }

    /**
     * 生成当前时间
     *
     * @return 返回消息对象的json字符串
     */
    public String generateCurrentMessage() {
        String msg = null;
        try {
            message.put("heartbeatVersion", String.valueOf(TimeUtil.currentTimeMillis()));
            message.put("lastHeartbeat", String.valueOf(TimeUtil.currentTimeMillis()));
            msg = JSONObject.toJSONString(message);
        } catch (JSONException e) {
            RecordLog.error("[HeartbeatMessage] Generate CurrentMessage failed " + e.toString());
        }
        return msg;
    }
}
