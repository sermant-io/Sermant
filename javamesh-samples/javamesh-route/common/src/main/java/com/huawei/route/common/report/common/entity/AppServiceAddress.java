/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.route.common.report.common.entity;

/**
 * description:应用的目标服务地址的数据模型
 *
 * @author wl
 * @since 2021-06-11
 */
public class AppServiceAddress extends TargetServiceAddress {
    public AppServiceAddress(String url, String ldc) {
        super(url, Type.APP, ldc);
    }

    public AppServiceAddress() {
        super();
        setType(Type.APP);
    }
}
