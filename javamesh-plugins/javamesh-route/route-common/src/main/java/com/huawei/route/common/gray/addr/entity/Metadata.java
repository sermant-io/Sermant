/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2021. All rights reserved.
 */

package com.huawei.route.common.gray.addr.entity;

/**
 * 额外元数据
 *
 * @author pengyuyi
 * @date 2021/10/26
 */
public class Metadata {
    /**
     * 接口版本
     */
    private String version;

    /**
     * nacos集群名称
     */
    private String clusterName;

    /**
     * 当前实例所属分组
     */
    private String group;

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }
}
