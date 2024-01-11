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

package com.huawei.metrics.entity;

/**
 * 公共指标信息
 *
 * @author zhp
 * @since 2023-10-18
 */
public class MetricsInfo {
    /**
     * 进程Id
     */
    private String processId;

    /**
     * 客户端IP
     */
    private String clientIp;

    /**
     * 服务端IP
     */
    private String serverIp;

    /**
     * 服务端端口
     */
    private String serverPort;

    /**
     * L4角色信息
     */
    private String l4Role;

    /**
     * L7角色信息
     */
    private String l7Role;

    /**
     * 协议
     */
    private String protocol;

    /**
     * 容器Id
     */
    private String containerId;

    /**
     * 进程名称
     */
    private String comm;

    /**
     * K8S pod名称
     */
    private String podName;

    /**
     * K8S pod的IP
     */
    private String podIp;

    /**
     * 是否开启SSL
     */
    private boolean enableSsl;

    /**
     * 节点实例Id
     */
    private String machineId;

    /**
     * URL信息
     */
    private String url;

    public String getProcessId() {
        return processId;
    }

    public void setProcessId(String processId) {
        this.processId = processId;
    }

    public String getClientIp() {
        return clientIp;
    }

    public void setClientIp(String clientIp) {
        this.clientIp = clientIp;
    }

    public String getServerIp() {
        return serverIp;
    }

    public void setServerIp(String serverIp) {
        this.serverIp = serverIp;
    }

    public String getServerPort() {
        return serverPort;
    }

    public void setServerPort(String serverPort) {
        this.serverPort = serverPort;
    }

    public String getL4Role() {
        return l4Role;
    }

    public void setL4Role(String l4Role) {
        this.l4Role = l4Role;
    }

    public String getL7Role() {
        return l7Role;
    }

    public void setL7Role(String l7Role) {
        this.l7Role = l7Role;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getContainerId() {
        return containerId;
    }

    public void setContainerId(String containerId) {
        this.containerId = containerId;
    }

    public String getComm() {
        return comm;
    }

    public void setComm(String comm) {
        this.comm = comm;
    }

    public String getPodName() {
        return podName;
    }

    public void setPodName(String podName) {
        this.podName = podName;
    }

    public String getPodIp() {
        return podIp;
    }

    public void setPodIp(String podIp) {
        this.podIp = podIp;
    }

    public boolean isEnableSsl() {
        return enableSsl;
    }

    public void setEnableSsl(boolean enableSsl) {
        this.enableSsl = enableSsl;
    }

    public String getMachineId() {
        return machineId;
    }

    public void setMachineId(String machineId) {
        this.machineId = machineId;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
