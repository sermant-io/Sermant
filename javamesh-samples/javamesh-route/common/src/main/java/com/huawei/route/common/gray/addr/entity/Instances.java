/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2021. All rights reserved.
 */

package com.huawei.route.common.gray.addr.entity;

import com.huawei.route.common.gray.label.entity.CurrentTag;

/**
 * 实例
 *
 * @author pengyuyi
 * @date 2021/10/15
 */
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