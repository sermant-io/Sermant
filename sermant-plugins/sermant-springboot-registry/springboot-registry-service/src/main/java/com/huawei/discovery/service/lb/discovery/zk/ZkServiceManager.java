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

package com.huawei.discovery.service.lb.discovery.zk;

import com.huawei.discovery.config.LbConfig;

import com.huaweicloud.sermant.core.plugin.config.PluginConfigManager;
import com.huaweicloud.sermant.core.plugin.service.PluginServiceManager;

import java.util.Locale;

/**
 * 用于选择zookeeper客户端服务{@link ZkService}, 实际基于不同的zk版本具有不同的实现, 参考{@link ZkService34},{@link ZkService35}
 *
 * @author zhouss
 * @since 2022-10-08
 */
public class ZkServiceManager {
    /**
     * 3.4版本前缀
     */
    private static final String VERSION_34_PREFIX = "3.4";

    private final LbConfig lbConfig;

    private ZkService zkService;

    /**
     * 构造器
     */
    public ZkServiceManager() {
        this.lbConfig = PluginConfigManager.getPluginConfig(LbConfig.class);
    }

    /**
     * 选择指定版本的服务
     *
     * @return ZkService
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
