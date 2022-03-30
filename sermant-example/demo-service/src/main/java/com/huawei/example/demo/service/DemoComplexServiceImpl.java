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

import com.huawei.example.demo.config.DemoConfig;
import com.huawei.example.demo.config.DemoServiceConfig;
import com.huawei.sermant.core.plugin.config.PluginConfigManager;
import com.huawei.sermant.core.plugin.service.PluginServiceManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 复杂服务示例实现
 *
 * @author HapThorin
 * @version 1.0.0
 * @since 2021-11-16
 */
public class DemoComplexServiceImpl implements DemoComplexService {
    private static final Logger LOGGER = LoggerFactory.getLogger("slf4j.test");

    @Override
    public void start() {
        LOGGER.error("[DemoComplexService]-start");
    }

    @Override
    public void stop() {
        LOGGER.error("[DemoComplexService]-stop");
    }

    @Override
    public void activeFunc() {
        LOGGER.error("[DemoComplexService]-activeFunc");
        final DemoSimpleService service = PluginServiceManager.getPluginService(DemoSimpleService.class);
        service.passiveFunc();
    }

    @Override
    public void passiveFunc() {
        LOGGER.error("[DemoComplexService]-passiveFunc");
        final DemoServiceConfig serviceConfig = PluginConfigManager.getPluginConfig(DemoServiceConfig.class);
        LOGGER.error(getClass().getSimpleName() + ": " + serviceConfig);
        final DemoConfig demoConfig = PluginConfigManager.getPluginConfig(DemoConfig.class);
        LOGGER.error(getClass().getSimpleName() + ": " + demoConfig);
    }
}
