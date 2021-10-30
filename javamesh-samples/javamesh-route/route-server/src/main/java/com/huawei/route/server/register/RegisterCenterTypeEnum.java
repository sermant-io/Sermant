/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.route.server.register;

/**
 * 支持的注册中心类型
 *
 * @author zhouss
 * @since 2021-10-13
 */
public enum RegisterCenterTypeEnum {
    /**
     * 注册中心-zookeeper
     */
    ZOOKEEPER,

    /**
     * 注册中心-nacos
     */
    NACOS,

    /**
     * 注册中心-SC
     */
    SERVICE_CENTER
}
