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

package com.huawei.registry.inject;

import com.huawei.registry.config.GraceConfig;

import com.huaweicloud.sermant.core.plugin.config.PluginConfigManager;
import com.huaweicloud.sermant.core.plugin.inject.ClassInjectDefine;

/**
 * 关闭时间监听器注入
 *
 * @author provenceee
 * @since 2022-05-25
 */
public class ContextClosedEventListenerInjectDefine implements ClassInjectDefine {
    @Override
    public String injectClassName() {
        return "com.huawei.registry.inject.grace.ContextClosedEventListener";
    }

    @Override
    public String factoryName() {
        return ENABLE_AUTO_CONFIGURATION_FACTORY_NAME;
    }

    @Override
    public boolean canInject() {
        GraceConfig graceConfig = PluginConfigManager.getPluginConfig(GraceConfig.class);
        return graceConfig.isEnableSpring() && graceConfig.isEnableGraceShutdown() && graceConfig
            .isEnableOfflineNotify();
    }

    @Override
    public Plugin plugin() {
        return Plugin.SPRING_REGISTRY_PLUGIN;
    }
}
