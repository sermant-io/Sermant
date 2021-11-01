/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.route.server.rules.enums;

/**
 * 数据传输渠道
 *
 * @author zhouss
 * @since 2021-10-13
 */
public enum SourceTypeEnum {
    /**
     * 针对requestHeader数据传输
     */
    HEADER,

    /**
     * 针对参数传输渠道
     * HTTP  请求体或者url
     * Dubbo RPCcontext或者url
     */
    PARAMETER,

    /**
     * Cookie数据获取
     */
    COOKIE,

    /**
     * Dubbo
     * 请求接口的第一个参数
     */
    ARG0,

    /**
     * Dubbo
     * 请求接口的第二个参数
     */
    ARG1,
    ARG2,
    ARG3,
    ARG4
}
