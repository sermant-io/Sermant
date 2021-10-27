/*
 * Copyright (C) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.apm.bootstrap.service.send;

/**
 * 统一网关客户端
 */
public interface UnifiedGatewayClient {

    /**
     * 向统一网关发送数据
     * @param data 数据字节
     * @param typeNum 数据类型号
     */
    void send(byte[] data, int typeNum);
}
