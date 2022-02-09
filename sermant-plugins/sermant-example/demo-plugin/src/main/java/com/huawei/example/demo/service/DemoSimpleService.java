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

import com.huawei.example.demo.common.DemoLogger;
import com.huawei.sermant.core.plugin.service.PluginService;
import com.huawei.sermant.core.plugin.service.PluginServiceManager;

/**
 * 示例服务，本示例中将展示如何编写一个插件服务
 *
 * @author HapThorin
 * @version 1.0.0
 * @since 2021-10-25
 */
public class DemoSimpleService implements PluginService {
    @Override
    public void start() {
        DemoLogger.println("[DemoSimpleService]-start");
    }

    @Override
    public void stop() {
        DemoLogger.println("[DemoSimpleService]-stop");
    }

    /**
     * 主动调用的方法，将调用{@link DemoComplexService#passiveFunc()}方法
     */
    public void activeFunc() {
        DemoLogger.println("[DemoSimpleService]-activeFunc");
        final DemoComplexService service = PluginServiceManager.getPluginService(DemoComplexService.class);
        service.passiveFunc();
    }

    /**
     * 被动调用的方法，将被{@link DemoComplexService#activeFunc()}方法调用
     */
    public void passiveFunc() {
        DemoLogger.println("[DemoSimpleService]-passiveFunc");
    }
}
