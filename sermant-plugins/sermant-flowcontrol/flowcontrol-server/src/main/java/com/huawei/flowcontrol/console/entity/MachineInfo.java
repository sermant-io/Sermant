/*
 * Copyright 1999-2020 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * Based on com/alibaba/csp/sentinel/dashboard/discovery/MachineInfo.java
 * from the Alibaba Sentinel project.
 */

package com.huawei.flowcontrol.console.entity;

import com.alibaba.csp.sentinel.util.StringUtil;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

@Getter
@Setter
public class MachineInfo implements Comparable<MachineInfo> {
    private String app = "";
    private Integer appType = 0;
    private String hostname = "";
    private String ip = "";
    private Integer port = -1;
    private long lastHeartbeat;
    private long heartbeatVersion;

    /**
     * Indicates the version of Sentinel client (since 0.2.0).
     */
    private String version;

    public static MachineInfo of(String app, String ip, Integer port) {
        MachineInfo machineInfo = new MachineInfo();
        machineInfo.setApp(app);
        machineInfo.setIp(ip);
        machineInfo.setPort(port);
        return machineInfo;
    }

    public boolean isHealthy() {
        long delta = System.currentTimeMillis() - lastHeartbeat;
        return delta < DashboardConfig.getUnhealthyMachineMillis();
    }

    /**
     * whether dead should be removed
     *
     * @return
     */
    public boolean isDead() {
        if (DashboardConfig.getAutoRemoveMachineMillis() > 0) {
            long delta = System.currentTimeMillis() - lastHeartbeat;
            return delta > DashboardConfig.getAutoRemoveMachineMillis();
        }
        return false;
    }

    public long getLastHeartbeat() {
        return lastHeartbeat;
    }

    public void setLastHeartbeat(long lastHeartbeat) {
        this.lastHeartbeat = lastHeartbeat;
    }

    @Override
    public int compareTo(MachineInfo o) {
        if (this == o) {
            return 0;
        }
        if (!port.equals(o.getPort())) {
            return port.compareTo(o.getPort());
        }
        if (!StringUtil.equals(app, o.getApp())) {
            return app.compareToIgnoreCase(o.getApp());
        }
        return ip.compareToIgnoreCase(o.getIp());
    }

    @Override
    public String toString() {
        return new StringBuilder("MachineInfo {")
            .append("app='").append(app).append('\'')
            .append(",appType='").append(appType).append('\'')
            .append(", hostname='").append(hostname).append('\'')
            .append(", ip='").append(ip).append('\'')
            .append(", port=").append(port)
            .append(", heartbeatVersion=").append(heartbeatVersion)
            .append(", lastHeartbeat=").append(lastHeartbeat)
            .append(", version='").append(version).append('\'')
            .append(", healthy=").append(isHealthy())
            .append('}').toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof MachineInfo)) {
            return false;
        }
        MachineInfo that = (MachineInfo) o;
        return Objects.equals(app, that.app)
            && Objects.equals(ip, that.ip)
            && Objects.equals(port, that.port);
    }

    @Override
    public int hashCode() {
        return Objects.hash(app, ip, port);
    }

    /**
     * Information for log
     *
     * @return
     */
    public String toLogString() {
        return app + "|" + ip + "|" + port + "|" + version;
    }
}
