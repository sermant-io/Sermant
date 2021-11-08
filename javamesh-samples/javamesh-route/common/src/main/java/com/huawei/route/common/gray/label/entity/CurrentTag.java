/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2021. All rights reserved.
 */

package com.huawei.route.common.gray.label.entity;

/**
 * 当前应用标签
 *
 * @author pengyuyi
 * @date 2021/10/27
 */
public class CurrentTag {
    /**
     * 当前应用版本号
     */
    private String version;

    /**
     * 当前应用ldc
     */
    private String ldc;

    public void setVersion(String version) {
        this.version = version;
    }

    public String getVersion() {
        return version;
    }

    public String getLdc() {
        return ldc;
    }

    public void setLdc(String ldc) {
        this.ldc = ldc;
    }
}