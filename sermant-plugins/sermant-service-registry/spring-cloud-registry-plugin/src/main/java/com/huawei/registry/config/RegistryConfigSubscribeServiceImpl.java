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

package com.huawei.registry.config;

import com.huawei.registry.entity.GraceShutdownHook;

import com.huaweicloud.sermant.core.common.LoggerFactory;
import com.huaweicloud.sermant.core.plugin.config.PluginConfigManager;
import com.huaweicloud.sermant.core.plugin.service.PluginService;
import com.huaweicloud.sermant.core.plugin.subscribe.ConfigSubscriber;
import com.huaweicloud.sermant.core.plugin.subscribe.CseGroupConfigSubscriber;
import com.huaweicloud.sermant.core.plugin.subscribe.DefaultGroupConfigSubscriber;
import com.huaweicloud.sermant.core.service.dynamicconfig.common.DynamicConfigEvent;
import com.huaweicloud.sermant.core.service.dynamicconfig.common.DynamicConfigListener;

import java.util.Locale;
import java.util.logging.Logger;

/**
 * Sign up for the configuration subscription service
 *
 * @author zhouss
 * @since 2022-05-24
 */
public class RegistryConfigSubscribeServiceImpl implements PluginService {
    /**
     * Subscription registration configuration
     *
     * @param serviceName Service name
     */
    public void subscribeRegistryConfig(String serviceName) {
        ConfigSubscriber subscriber;
        final RegisterConfig registerConfig = PluginConfigManager.getPluginConfig(RegisterConfig.class);
        final RegisterServiceCommonConfig registerCommonConfig =
                PluginConfigManager.getPluginConfig(RegisterServiceCommonConfig.class);
        if (registerCommonConfig.getRegisterType() == RegisterType.SERVICE_COMB
                && registerConfig.isEnableSpringRegister()) {
            // CSE was used
            subscriber = new CseGroupConfigSubscriber(serviceName, new RegistryConfigListener(),
                    "SpringCloudRegistry");
        } else {
            // Other scenarios
            subscriber = new DefaultGroupConfigSubscriber(serviceName, new RegistryConfigListener(),
                    "SpringCloudRegistry");
        }
        subscriber.subscribe();
        fixGrace();
    }

    private void fixGrace() {
        // Modify the elegant online and offline switches based on environment variables,
        // and this configuration has the highest priority
        final GraceConfig graceConfig = PluginConfigManager.getPluginConfig(GraceConfig.class);
        graceConfig.fixGraceSwitch();
        if (graceConfig.isEnableSpring() && graceConfig.isEnableGraceShutdown()) {
            Runtime.getRuntime().addShutdownHook(new GraceShutdownHook());
        }
    }

    /**
     * Register and configure a listener
     *
     * @since 2022-05-24
     */
    static class RegistryConfigListener implements DynamicConfigListener {
        private static final Logger LOGGER = LoggerFactory.getLogger();

        private final RegistryConfigResolver graceConfigResolver = new GraceConfigResolver();

        private final RegistryConfigResolver switchConfigResolver = new OriginRegistrySwitchConfigResolver();

        @Override
        public void process(DynamicConfigEvent event) {
            LOGGER.info(String.format(Locale.ENGLISH, "[Registry] Received Config [%s], content: [%s], operate: [%s]",
                    event.getKey(), event.getContent(), event.getEventType()));
            switchConfigResolver.updateConfig(event);
            graceConfigResolver.updateConfig(event);
        }
    }
}
