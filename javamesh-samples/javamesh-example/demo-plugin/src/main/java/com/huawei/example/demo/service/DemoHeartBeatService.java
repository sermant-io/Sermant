/*
 * Copyright (C) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.example.demo.service;

import com.huawei.apm.core.service.ServiceManager;
import com.huawei.apm.core.plugin.service.PluginService;
import com.huawei.apm.core.service.heartbeat.HeartbeatService;

/**
 * 本示例中，将展示如何在插件服务中使用心跳功能
 *
 * @author HapThorin
 * @version 1.0.0
 * @since 2021/10/25
 */
public class DemoHeartBeatService implements PluginService {
    private static final HeartbeatService HEARTBEAT_SERVICE = ServiceManager.getService(HeartbeatService.class);

    @Override
    public void start() {
        // 注册心跳功能
        HEARTBEAT_SERVICE.heartbeat("demo_heartbeat");
    }

    @Override
    public void stop() {
        // 停止心跳功能
        HEARTBEAT_SERVICE.stopHeartbeat("demo_heartbeat");
    }
}
