/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2021. All rights reserved.
 */

package com.huawei.route.common.gray.label.entity;

/**
 * 路由标签
 *
 * @author pengyuyi
 * @date 2021/10/23
 */
public class Tags {
    /**
     * 路由的应用版本
     */
    private String version;

    /**
     * appId
     */
    private String app;

    public void setVersion(String version) {
        this.version = version;
    }

    public String getVersion() {
        return this.version;
    }

    public void setApp(String app) {
        this.app = app;
    }

    public String getApp() {
        return this.app;
    }
}
