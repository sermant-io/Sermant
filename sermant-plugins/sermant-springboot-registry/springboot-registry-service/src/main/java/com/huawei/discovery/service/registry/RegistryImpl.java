/*
 * Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huawei.discovery.service.registry;

import com.huawei.discovery.config.DiscoveryPluginConfig;
import com.huawei.discovery.entity.ServiceInstance;
import com.huawei.discovery.service.RegistryService;
import com.huawei.discovery.service.lb.DiscoveryManager;

import com.huaweicloud.sermant.core.common.LoggerFactory;
import com.huaweicloud.sermant.core.plugin.config.PluginConfigManager;
import com.huaweicloud.sermant.core.service.ServiceManager;
import com.huaweicloud.sermant.core.service.heartbeat.api.HeartbeatService;

import java.io.IOException;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 注册
 *
 * @author zhouss
 * @since 2022-09-27
 */
public class RegistryImpl implements RegistryService {
    private static final Logger LOGGER = LoggerFactory.getLogger();

    private final AtomicBoolean isShutdown = new AtomicBoolean();

    private HeartbeatService heartbeatService;

    @Override
    public void start() {
        try {
            heartbeatService = ServiceManager.getService(HeartbeatService.class);
        } catch (IllegalArgumentException ex) {
            // ignored 不存在心跳服务
        }
    }

    @Override
    public void registry(ServiceInstance serviceInstance) {
        DiscoveryManager.INSTANCE.registry(serviceInstance);
    }

    @Override
    public void shutdown() {
        if (!PluginConfigManager.getPluginConfig(DiscoveryPluginConfig.class).isEnableRegistry()) {
            return;
        }
        if (isShutdown.compareAndSet(false, true)) {
            try {
                DiscoveryManager.INSTANCE.stop();
            } catch (IOException ex) {
                LOGGER.log(Level.WARNING, "Stop lb service failed!", ex);
            } finally {
                addStatusForHeartbeat();
            }
        }
    }

    private void addStatusForHeartbeat() {
        if (heartbeatService == null) {
            LOGGER.warning("Heartbeat service is not init when add stop status for heartbeat!");
            return;
        }
        heartbeatService.setExtInfo(() -> Collections.singletonMap("status", "stopped"));
    }

    @Override
    public void stop() {
        shutdown();
    }
}
