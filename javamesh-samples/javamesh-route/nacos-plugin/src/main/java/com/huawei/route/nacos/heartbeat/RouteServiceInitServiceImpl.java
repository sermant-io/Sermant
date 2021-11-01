/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.route.nacos.heartbeat;

import com.huawei.apm.bootstrap.boot.CoreServiceManager;
import com.huawei.apm.bootstrap.boot.PluginService;
import com.huawei.apm.bootstrap.boot.heartbeat.HeartbeatInterval;
import com.huawei.apm.bootstrap.boot.heartbeat.HeartbeatService;

/**
 * 路由服务初始化类，暂定放到此处，等后续route-common迁移，放到该模块初始化
 *
 * @author zhouss
 * @since 2021-11-01
 */
public class RouteServiceInitServiceImpl implements PluginService {
    /**
     * 标签库心跳名称
     */
    private static final String HEARTBEAT_NAME = "TAG_HEARTBEAT";

    private HeartbeatService heartbeatService;

    @Override
    public void init() {
        heartbeatService = CoreServiceManager.INSTANCE.getService(HeartbeatService.class);
        heartbeatService.heartbeat(HEARTBEAT_NAME,
                HeartbeatInfoProvider.getInstance(),
                HeartbeatInterval.OFTEN);
    }

    @Override
    public void stop() {
        heartbeatService.stopHeartbeat(HEARTBEAT_NAME);
    }
}
