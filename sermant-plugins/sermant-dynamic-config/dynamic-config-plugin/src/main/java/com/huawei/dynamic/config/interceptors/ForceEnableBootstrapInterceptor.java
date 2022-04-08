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
import com.huawei.sermant.core.plugin.agent.entity.ExecuteContext;
import com.huawei.sermant.core.plugin.config.PluginConfigManager;

import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.SimpleCommandLinePropertySource;

/**
 * 强制开启启动配置
 *
 * @author zhouss
 * @since 2022-04-13
 */
public class ForceEnableBootstrapInterceptor extends DynamicConfigSwitchSupport {
    private static final String FORCE_BOOTSTRAP_NAME = "force-enable-bootstrap";

    private static final String ENABLE_BOOTSTRAP_CONFIG = "--spring.cloud.bootstrap.enabled=true";

    @Override
    public ExecuteContext before(ExecuteContext context) {
        return context;
    }

    @Override
    public ExecuteContext doAfter(ExecuteContext context) {
        final Object argument = context.getArguments()[0];
        if (PluginConfigManager.getPluginConfig(DynamicConfiguration.class).isForceEnableBootstrap()
            && argument instanceof ConfigurableEnvironment) {
            ConfigurableEnvironment environment = (ConfigurableEnvironment) argument;
            environment.getPropertySources()
                .addFirst(
                    new SimpleCommandLinePropertySource(FORCE_BOOTSTRAP_NAME, new String[]{ENABLE_BOOTSTRAP_CONFIG}));
        }
        return context;
    }
}
