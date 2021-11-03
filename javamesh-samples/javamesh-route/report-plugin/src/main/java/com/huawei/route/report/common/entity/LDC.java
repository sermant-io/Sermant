/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.route.report.common.entity;

/**
 * 服务所属单元
 *
 * @author wl
 * @since 2021-06-11
 */
public class LDC {
    private String name;

    public LDC() {
    }

    public LDC(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
