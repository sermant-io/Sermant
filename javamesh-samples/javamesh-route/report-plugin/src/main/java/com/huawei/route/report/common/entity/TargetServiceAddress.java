/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.route.report.common.entity;

import com.alibaba.fastjson.annotation.JSONField;

/**
 * 目标服务地址
 *
 * @author wl
 * @since 2021-06-11
 */
public class TargetServiceAddress {

    private String url;

    private Type type;

    private String ldc;

    @JSONField(name = "type")
    public String getTypeValue() {
        return type.getName();
    }

    @JSONField(name = "type")
    public void setTypeValue(String type) {
        this.type = Type.getEnum(type);
    }

    @JSONField(serialize = false)
    public Type getType() {
        return type;
    }

    @JSONField(serialize = false)
    public void setType(Type type) {
        this.type = type;
    }

    public TargetServiceAddress(String url, Type type, String ldc) {
        this.url = url;
        this.type = type;
    }

    public TargetServiceAddress() {
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getLdc() {
        return ldc;
    }

    public void setLdc(String ldc) {
        this.ldc = ldc;
    }
}
