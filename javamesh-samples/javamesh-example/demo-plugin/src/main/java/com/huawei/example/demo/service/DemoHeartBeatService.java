/*
 * Copyright (C) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.example.demo.service;

import java.util.Collections;
import java.util.Map;

import com.huawei.apm.core.plugin.service.PluginService;
import com.huawei.apm.core.service.ServiceManager;
import com.huawei.apm.core.service.heartbeat.HeartbeatService;
import com.huawei.apm.core.service.heartbeat.ExtInfoProvider;

/**
 * 本示例中，将展示如何在插件服务中使用心跳功能
 *
 * @author HapThorin
 * @version 1.0.0
 * @since 2021/10/25
 */
public class DemoHeartBeatService implements PluginService {
    @Override
    public void start() {
        final HeartbeatService service = ServiceManager.getService(HeartbeatService.class);
        service.setExtInfo("example", new ExtInfoProvider() {
            @Override
            public Map<String, String> getExtInfo() {
                return Collections.singletonMap("exampleKey", "exampleValue");
            }
        });
    }

    @Override
    public void stop() {
    }
}
