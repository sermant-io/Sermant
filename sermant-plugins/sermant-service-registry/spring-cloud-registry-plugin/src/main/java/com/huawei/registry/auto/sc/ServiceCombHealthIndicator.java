/*
 * Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.huawei.registry.auto.sc;

import com.huawei.registry.services.RegisterCenterService;

import com.huaweicloud.sermant.core.plugin.service.PluginServiceManager;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;
import org.springframework.cloud.client.discovery.health.DiscoveryHealthIndicator;

/**
 * 健康检查 配合spring actuator监控使用
 *
 * @author zhouss
 * @since 2022-05-19
 */
public class ServiceCombHealthIndicator implements DiscoveryHealthIndicator {
    private RegisterCenterService registerCenterService;

    @Override
    public String getName() {
        return "Service Center";
    }

    @Override
    public Health health() {
        if (registerCenterService == null) {
            registerCenterService = PluginServiceManager.getPluginService(RegisterCenterService.class);
        }
        return Health.status(new Status(registerCenterService.getRegisterCenterStatus(), "Service Center is alive"))
            .build();
    }
}
