/*
 * Copyright (C) 2021-2021 Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.huawei.example.demo.service;

import com.huawei.sermant.core.plugin.service.PluginService;
import com.huawei.sermant.core.service.ServiceManager;
import com.huawei.sermant.core.service.heartbeat.ExtInfoProvider;
import com.huawei.sermant.core.service.heartbeat.HeartbeatService;

import java.util.Collections;
import java.util.Map;

/**
 * 本示例中，将展示如何在插件服务中使用心跳功能
 *
 * @author HapThorin
 * @version 1.0.0
 * @since 2021-10-25
 */
public class DemoHeartBeatService implements PluginService {
    @Override
    public void start() {
        final HeartbeatService service = ServiceManager.getService(HeartbeatService.class);
        service.setExtInfo(new ExtInfoProvider() {
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
