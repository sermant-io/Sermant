/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.skywalking.oap.server.receiver.server.monitor.collection.module;

import com.huawei.skywalking.oap.server.receiver.server.monitor.collection.service.ServerMonitorQueryService;

import org.apache.skywalking.oap.server.library.module.ModuleDefine;

/**
 * 服务器监控模块
 *
 * @author zhengbin zhao
 * @since 2021-02-25
 */
public class ServerMonitorModule extends ModuleDefine {
    /**
     * 模块名称
     */
    public static final String MODULE_NAME = "receiver-server-monitor";

    public ServerMonitorModule() {
        super(MODULE_NAME);
    }

    @Override

    public Class[] services() {
        return new Class[]{
            ServerMonitorQueryService.class
        };
    }
}
