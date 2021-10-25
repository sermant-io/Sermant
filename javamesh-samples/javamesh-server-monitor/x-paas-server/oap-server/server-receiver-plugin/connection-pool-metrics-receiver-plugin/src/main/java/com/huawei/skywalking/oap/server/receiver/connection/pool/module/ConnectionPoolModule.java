/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2021. All rights reserved.
 */

package com.huawei.skywalking.oap.server.receiver.connection.pool.module;

import com.huawei.skywalking.oap.server.receiver.connection.pool.service.DruidQueryService;

import org.apache.skywalking.oap.server.library.module.ModuleDefine;

/**
 * 定义数据源指标接收模块
 *
 * @author zhouss
 * @since 2020-12-02
 **/
public class ConnectionPoolModule extends ModuleDefine {
    /**
     * 模块名称
     */
    public static final String NAME = "receiver-connection-pool";

    public ConnectionPoolModule() {
        super(NAME);
    }

    @Override
    public Class[] services() {
        return new Class[]{
            DruidQueryService.class
        };
    }
}
