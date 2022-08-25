/*
 * Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.huawei.dynamic.config.interceptors;

import com.huawei.dynamic.config.DynamicConfiguration;

import com.huaweicloud.sermant.core.plugin.agent.entity.ExecuteContext;
import com.huaweicloud.sermant.core.plugin.config.PluginConfigManager;

/**
 * 拦截loadFactories注入自定义配置源
 *
 * @author zhouss
 * @since 2022-04-08
 */
public class ZookeeperLocatorInterceptor extends DynamicConfigSwitchSupport {
    private final DynamicConfiguration configuration;

    /**
     * 构造器
     */
    public ZookeeperLocatorInterceptor() {
        configuration = PluginConfigManager.getPluginConfig(DynamicConfiguration.class);
    }

    @Override
    public ExecuteContext doBefore(ExecuteContext context) {
        if (!configuration.isEnableOriginConfigCenter() || isDynamicClosed()) {
            context.skip(null);
        }
        return context;
    }
}
