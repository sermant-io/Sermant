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

package io.sermant.dynamic.config.init;

import io.sermant.core.common.LoggerFactory;
import io.sermant.core.config.ConfigManager;
import io.sermant.core.plugin.config.PluginConfigManager;
import io.sermant.core.plugin.config.ServiceMeta;
import io.sermant.core.plugin.service.PluginService;
import io.sermant.core.plugin.subscribe.ConfigSubscriber;
import io.sermant.core.plugin.subscribe.CseGroupConfigSubscriber;
import io.sermant.core.plugin.subscribe.DefaultGroupConfigSubscriber;
import io.sermant.dynamic.config.DynamicConfiguration;
import io.sermant.dynamic.config.entity.ClientMeta;
import io.sermant.dynamic.config.subscribe.ConfigListener;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * initialize the configuration center
 *
 * @author zhouss
 * @since 2022-04-13
 */
public class DynamicConfigInitializer implements PluginService {
    private final AtomicBoolean isStarted = new AtomicBoolean();

    /**
     * the initialization task is started
     */
    public void doStart() {
        if (isStarted.compareAndSet(false, true)) {
            run();
        }
    }

    private void run() {
        final DynamicConfiguration pluginConfig = PluginConfigManager.getPluginConfig(DynamicConfiguration.class);
        ConfigSubscriber subscriber;
        if (pluginConfig.isEnableCseAdapter()) {
            fillCseMeta();
            subscriber = new CseGroupConfigSubscriber(ClientMeta.INSTANCE.getServiceName(), new ConfigListener(),
                    "DynamicConfig");
        } else {
            subscriber = new DefaultGroupConfigSubscriber(ClientMeta.INSTANCE.getServiceName(),
                new ConfigListener(), "DynamicConfig");
        }
        if (subscriber.subscribe()) {
            LoggerFactory.getLogger().info("[DynamicConfig] Subscribe config center successfully!");
        } else {
            LoggerFactory.getLogger().severe("[DynamicConfig] Subscribe config center failed!");
        }
    }

    private void fillCseMeta() {
        final ServiceMeta config = ConfigManager.getConfig(ServiceMeta.class);
        ClientMeta.INSTANCE.setApplication(config.getApplication());
        ClientMeta.INSTANCE.setEnvironment(config.getEnvironment());
    }
}
