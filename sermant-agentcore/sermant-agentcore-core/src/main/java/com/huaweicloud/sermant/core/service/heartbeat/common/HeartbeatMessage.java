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

package com.huaweicloud.sermant.core.service.heartbeat.common;

import com.huaweicloud.sermant.core.common.BootArgsIndexer;
import com.huaweicloud.sermant.core.utils.NetworkUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * HeartbeatMessage
 *
 * @author luanwenfei
 * @since 2022-03-19
 */
public class HeartbeatMessage {
    private String hostName;

    private List<String> ip;

    private final String service;

    private final String appType;

    private long heartbeatTime;

    private long lastHeartbeatTime;

    private final String version;

    private final String instanceId;

    private final Map<String, PluginInfo> pluginInfoMap = new HashMap<>();

    /**
     * constructor
     */
    public HeartbeatMessage() {
        this.hostName = NetworkUtils.getHostName().orElse(null);
        this.ip = NetworkUtils.getAllNetworkIp();
        this.service = BootArgsIndexer.getServiceName();
        this.appType = BootArgsIndexer.getAppType();
        this.heartbeatTime = System.currentTimeMillis();
        this.lastHeartbeatTime = System.currentTimeMillis();
        this.version = BootArgsIndexer.getCoreVersion();
        this.instanceId = BootArgsIndexer.getInstanceId();
    }

    /**
     * update Heartbeat Version
     */
    public void updateHeartbeatVersion() {
        this.lastHeartbeatTime = this.heartbeatTime;
        this.heartbeatTime = System.currentTimeMillis();
        this.hostName = NetworkUtils.getHostName().orElse(null);
        this.ip = NetworkUtils.getAllNetworkIp();
    }

    public Map<String, PluginInfo> getPluginInfoMap() {
        return pluginInfoMap;
    }

    public String getHostName() {
        return hostName;
    }

    public List<String> getIp() {
        return ip;
    }

    public String getService() {
        return service;
    }

    public String getAppType() {
        return appType;
    }

    public long getHeartbeatTime() {
        return heartbeatTime;
    }

    public long getLastHeartbeatTime() {
        return lastHeartbeatTime;
    }

    public String getVersion() {
        return version;
    }

    public String getInstanceId() {
        return instanceId;
    }
}
