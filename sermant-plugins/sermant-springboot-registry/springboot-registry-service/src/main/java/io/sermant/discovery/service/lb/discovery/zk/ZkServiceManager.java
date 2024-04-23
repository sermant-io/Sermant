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

package io.sermant.discovery.service.lb.discovery.zk;

import io.sermant.core.plugin.config.PluginConfigManager;
import io.sermant.core.plugin.service.PluginServiceManager;
import io.sermant.discovery.config.LbConfig;

import java.util.Locale;

/**
 * Used to select the zookeeper client service {@link ZkService}, which actually has different implementations based on
 * different zk versions, refer to {@link ZkService34},{@link ZkService35}
 *
 * @author zhouss
 * @since 2022-10-08
 */
public class ZkServiceManager {
    /**
     * 3.4 version prefix
     */
    private static final String VERSION_34_PREFIX = "3.4";

    private final LbConfig lbConfig;

    private volatile ZkService zkService;

    /**
     * Constructor
     */
    public ZkServiceManager() {
        this.lbConfig = PluginConfigManager.getPluginConfig(LbConfig.class);
    }

    /**
     * Select the specified version of the service
     *
     * @return ZkService
     * @throws IllegalArgumentException Unable to get zk service throw
     */
    public ZkService chooseService() {
        if (zkService != null) {
            return zkService;
        }
        synchronized (this) {
            if (zkService == null) {
                final String version = lbConfig.getZkServerVersion();
                if (version.startsWith(VERSION_34_PREFIX)) {
                    zkService = PluginServiceManager.getPluginService(ZkService34.class);
                }
            }
        }
        if (zkService == null) {
            throw new IllegalArgumentException(String.format(Locale.ENGLISH,
                    "Can not get target zookeeper client version(%s) service", lbConfig.getZkServerVersion()));
        }
        return zkService;
    }
}
