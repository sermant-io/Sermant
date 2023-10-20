/*
 * Copyright (C) 2023-2023 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huawei.jsse.declarer;

import com.huawei.jsse.config.JsseConfig;

import com.huaweicloud.sermant.core.plugin.agent.declarer.PluginDeclarer;
import com.huaweicloud.sermant.core.plugin.agent.declarer.SuperTypeDeclarer;
import com.huaweicloud.sermant.core.plugin.config.PluginConfigManager;

/**
 * 插件增强声明
 *
 * @author zhp
 * @since 2023-10-17
 */
public abstract class AbstractDeclarer implements PluginDeclarer {
    private static final JsseConfig JSSE_CONFIG = PluginConfigManager.getPluginConfig(JsseConfig.class);

    @Override
    public SuperTypeDeclarer[] getSuperTypeDeclarers() {
        return new SuperTypeDeclarer[0];
    }

    @Override
    public boolean isEnabled() {
        return JSSE_CONFIG.isEnable();
    }
}
