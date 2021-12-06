/*
 * Copyright (C) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved
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

package com.huawei.javamesh.backend.service;

import com.huawei.javamesh.backend.pojo.Message;

import java.util.HashMap;
import java.util.Map;

public enum SendServiceManager {
    INSTANCE;

    private final Map<Integer, SendService> sendServices = new HashMap<>();

    SendServiceManager() {
        sendServices.put(Message.ServiceData.DataType.SERVICE_HEARTBEAT_VALUE, new SendHeartbeat());
        sendServices.put(Message.ServiceData.DataType.LOG_VALUE, new SendLog());
        sendServices.put(Message.ServiceData.DataType.PLUGIN_FLOW_CONTROL_DATA_VALUE, new SendFlowControl());
        sendServices.put(Message.ServiceData.DataType.PLUGIN_FLOW_RECORD_DATA_VALUE, new SendFlowRecord());
        sendServices.put(Message.ServiceData.DataType.SERVER_MONITOR_VALUE, new SendServerMonitor());
        sendServices.put(Message.ServiceData.DataType.ORACLE_JVM_MONITOR_VALUE, new SendOracleJvmMonitor());
        sendServices.put(Message.ServiceData.DataType.IBM_JVM_MONITOR_VALUE, new SendIbmJvmMonitor());
        sendServices.put(Message.ServiceData.DataType.AGENT_REGISTRATION_VALUE, new SendAgentRegistration());
        sendServices.put(Message.ServiceData.DataType.DRUID_MONITOR_VALUE, new SendDruidMonitor());
        sendServices.put(Message.ServiceData.DataType.AGENT_MONITOR_VALUE, new SendAgentMonitor());
        sendServices.put(Message.ServiceData.DataType.AGENT_SPAN_EVENT_VALUE, new SendAgentSpanEvent());
    }

    public Map<Integer, SendService> getSendServices() {
        return sendServices;
    }
}
