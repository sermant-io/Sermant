/*
 * Copyright (C) 2022-2024 Sermant Authors. All rights reserved.
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

import com.huaweicloud.sermant.core.common.LoggerFactory;
import com.huaweicloud.sermant.core.plugin.agent.entity.ExecuteContext;
import com.huaweicloud.sermant.core.plugin.config.PluginConfigManager;

import org.springframework.cloud.bootstrap.config.BootstrapPropertySource;

import java.util.Locale;
import java.util.logging.Logger;

/**
 * When a user sends a configuration in the configuration center, the native configuration center is dynamically closed,
 * The intercept point starts to block the native configuration center configuration source and
 * prevents the configuration from taking effect
 * effectiveView{@link com.huawei.dynamic.config.source.OriginConfigCenterDisableListener}
 * the forbidden configuration source was added
 *
 * @author zhouss
 * @since 2022-04-08
 */
public class MutableSourceInterceptor extends DynamicConfigSwitchSupport {
    private static final Logger LOGGER = LoggerFactory.getLogger();

    private static final String ZOOKEEPER_SOURCE = "org.springframework.cloud.zookeeper.config.ZookeeperPropertySource";

    private static final String NACOS_SOURCE = "com.alibaba.cloud.nacos.client.NacosPropertySource";

    private static final String BOOTSTRAP_SOURCE = "org.springframework.cloud.bootstrap.config.BootstrapPropertySource";

    private final DynamicConfiguration configuration;

    /**
     * constructor
     */
    public MutableSourceInterceptor() {
        configuration = PluginConfigManager.getPluginConfig(DynamicConfiguration.class);
    }

    @Override
    public ExecuteContext doBefore(ExecuteContext context) {
        if (!configuration.isEnableOriginConfigCenter() || !isDynamicClosed()) {
            return context;
        }
        final Object source = context.getArguments()[0];
        if (!BOOTSTRAP_SOURCE.equals(source.getClass().getName())) {
            return context;
        }
        BootstrapPropertySource<?> bootstrapPropertySource = (BootstrapPropertySource<?>) source;
        if (isTargetSource(bootstrapPropertySource.getDelegate())) {
            LOGGER.info(String.format(Locale.ENGLISH,
                    "Ignored source [%s] because of origin config center's has been closed!", source));
            context.skip(null);
        }
        return context;
    }

    private boolean isTargetSource(Object source) {
        final String name = source.getClass().getName();
        return ZOOKEEPER_SOURCE.equals(name) || NACOS_SOURCE.equals(name);
    }
}
