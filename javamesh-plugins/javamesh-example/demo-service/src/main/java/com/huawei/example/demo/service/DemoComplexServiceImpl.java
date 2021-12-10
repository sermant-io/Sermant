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

import java.util.logging.Logger;

import com.huawei.javamesh.core.common.LoggerFactory;
import com.huawei.javamesh.core.plugin.config.PluginConfigManager;
import com.huawei.javamesh.core.plugin.service.PluginServiceManager;
import com.huawei.example.demo.common.DemoLogger;
import com.huawei.example.demo.config.DemoConfig;
import com.huawei.example.demo.config.DemoServiceConfig;

/**
 * 复杂服务示例实现
 *
 * @author HapThorin
 * @version 1.0.0
 * @since 2021/11/16
 */
public class DemoComplexServiceImpl implements DemoComplexService {
    @Override
    public void start() {
        DemoLogger.println("[DemoComplexService]-start");
    }

    @Override
    public void stop() {
        DemoLogger.println("[DemoComplexService]-stop");
    }

    @Override
    public void activeFunc() {
        DemoLogger.println("[DemoComplexService]-activeFunc");
        final DemoSimpleService service = PluginServiceManager.getPluginService(DemoSimpleService.class);
        service.passiveFunc();
    }

    @Override
    public void passiveFunc() {
        DemoLogger.println("[DemoComplexService]-passiveFunc");
        final DemoServiceConfig serviceConfig = PluginConfigManager.getPluginConfig(DemoServiceConfig.class);
        DemoLogger.println(getClass().getSimpleName() + ": " + serviceConfig);
        final DemoConfig demoConfig = PluginConfigManager.getPluginConfig(DemoConfig.class);
        DemoLogger.println(getClass().getSimpleName() + ": " + demoConfig);
    }
}
