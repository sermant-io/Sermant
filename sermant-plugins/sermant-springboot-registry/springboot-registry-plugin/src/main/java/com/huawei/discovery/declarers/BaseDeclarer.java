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

package com.huawei.discovery.declarers;

import com.huawei.discovery.config.DiscoveryPluginConfig;

import com.huaweicloud.sermant.core.plugin.agent.declarer.AbstractPluginDeclarer;
import com.huaweicloud.sermant.core.plugin.config.PluginConfigManager;

/**
 * 基础拦截点声明, 主要用于declarer加载开关
 *
 * @author zhouss
 * @since 2022-11-03
 */
public abstract class BaseDeclarer extends AbstractPluginDeclarer {
    @Override
    public boolean isEnabled() {
        return PluginConfigManager.getPluginConfig(DiscoveryPluginConfig.class).isEnableRegistry();
    }
}
