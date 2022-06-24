/*
 * Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.huawei.nacos.rest.consumer.entity;

/**
 * 响应信息
 *
 * @author zhouss
 * @since 2022-06-20
 */
public class ResponseInfo {
    private String port;

    private String ip;

    private String serviceName;

    private int qps;

    private boolean open;

    /**
     * 构造器
     *
     * @param port 端口
     * @param ip ip地址
     * @param serviceName 服务名
     * @param open 是否开启预热
     * @param qps qps
     */
    public ResponseInfo(String port, String ip, String serviceName, boolean open, int qps) {
        this.port = port;
        this.ip = ip;
        this.serviceName = serviceName;
        this.open = open;
        this.qps = qps;
    }

    public int getQps() {
        return qps;
    }

    public void setQps(int qps) {
        this.qps = qps;
    }

    public boolean isOpen() {
        return open;
    }

    public void setOpen(boolean open) {
        this.open = open;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }
}
