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

package com.huawei.route.common.gray.addr.entity;

import com.huawei.route.common.gray.label.entity.CurrentTag;

/**
 * 实例
 *
 * @author provenceee
 * @since 2021/10/15
 */
@SuppressWarnings("checkstyle:RegexpSingleline")
public class Instances {
    /**
     * ldc
     */
    private String ldc;

    /**
     * 服务名
     */
    private String serviceName;

    /**
     * ip
     */
    private String ip;

    /**
     * 端口
     */
    private int port;

    /**
     * 额外元数据
     */
    private Metadata metadata;

    /**
     * 当前实例标签
     */
    private CurrentTag currentTag;

    /**
     * 是否有效
     */
    private boolean valid;

    /**
     * 该实例是否健康（目前以注册中心的健康状态为准）
     */
    private boolean health;

    public String getLdc() {
        return ldc;
    }

    public void setLdc(String ldc) {
        this.ldc = ldc;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public Metadata getMetadata() {
        return metadata;
    }

    public void setMetadata(Metadata metadata) {
        this.metadata = metadata;
    }

    public CurrentTag getCurrentTag() {
        return currentTag;
    }

    public void setCurrentTag(CurrentTag currentTag) {
        this.currentTag = currentTag;
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public boolean isHealth() {
        return health;
    }

    public void setHealth(boolean health) {
        this.health = health;
    }
}