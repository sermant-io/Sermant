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

package com.huawei.gray.feign.service;

import com.huawei.route.common.gray.config.GrayConfig;
import com.huawei.route.common.gray.listener.GrayDynamicConfigListener;
import com.huawei.sermant.core.plugin.config.PluginConfigManager;
import com.huawei.sermant.core.plugin.service.PluginService;
import com.huawei.sermant.core.service.ServiceManager;
import com.huawei.sermant.core.service.dynamicconfig.DynamicConfigService;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 配置服务
 *
 * @author fuziye
 * @date 2021/12/29
 */
public class ConfigServiceImpl implements PluginService {
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    private GrayConfig grayConfig;

    private GrayDynamicConfigListener listener;

    private DynamicConfigService configurationService;

    /**
     * 初始化通知
     */
    @Override
    public void start() {
        grayConfig = PluginConfigManager.getPluginConfig(GrayConfig.class);
        configurationService = ServiceManager.getService(DynamicConfigService.class);
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                initRequests();
            }
        });
    }

    private void initRequests() {
        listener = new GrayDynamicConfigListener(grayConfig.getSpringCloudKey(), grayConfig.getSpringCloudKey());
        configurationService.addGroupListener(grayConfig.getSpringCloudGroup(), listener, true);
    }

    @Override
    public void stop() {
        configurationService.removeGroupListener(grayConfig.getSpringCloudGroup());
    }
}