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

package com.huawei.loadbalancer.service;

import com.huawei.loadbalancer.config.LoadbalancerConfig;
import com.huawei.loadbalancer.listener.LoadbalancerConfigListener;
import com.huawei.sermant.core.plugin.config.PluginConfigManager;
import com.huawei.sermant.core.plugin.service.PluginService;
import com.huawei.sermant.core.service.ServiceManager;
import com.huawei.sermant.core.service.dynamicconfig.DynamicConfigService;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 负载均衡基础配置类
 *
 * @author provenceee
 * @since 2022-01-22
 */
public class LoadbalancerConfigService implements PluginService {
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    private LoadbalancerConfig config;

    private DynamicConfigService configService;

    /**
     * 初始化通知
     */
    @Override
    public void start() {
        config = PluginConfigManager.getPluginConfig(LoadbalancerConfig.class);
        configService = ServiceManager.getService(DynamicConfigService.class);
        executorService.execute(this::initRequests);
    }

    private void initRequests() {
        configService.addGroupListener(config.getGroup(), new LoadbalancerConfigListener(config), true);
    }

    @Override
    public void stop() {
        if (configService != null) {
            configService.removeGroupListener(config.getGroup());
        }
    }
}