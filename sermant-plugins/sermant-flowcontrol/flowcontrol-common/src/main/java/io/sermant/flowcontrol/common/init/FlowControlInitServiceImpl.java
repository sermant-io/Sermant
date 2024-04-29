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

package io.sermant.flowcontrol.common.init;

import io.sermant.core.common.LoggerFactory;
import io.sermant.core.operation.OperationManager;
import io.sermant.core.operation.initializer.DynamicConfigServiceInitializer;
import io.sermant.core.plugin.config.PluginConfigManager;
import io.sermant.core.plugin.service.PluginService;
import io.sermant.core.plugin.subscribe.ConfigSubscriber;
import io.sermant.core.plugin.subscribe.CseGroupConfigSubscriber;
import io.sermant.core.plugin.subscribe.DefaultGroupConfigSubscriber;
import io.sermant.core.service.ServiceManager;
import io.sermant.core.service.dynamicconfig.DynamicConfigService;
import io.sermant.flowcontrol.common.config.FlowControlConfig;
import io.sermant.flowcontrol.common.core.match.MatchManager;
import io.sermant.flowcontrol.common.core.rule.RuleDynamicConfigListener;
import io.sermant.flowcontrol.common.entity.FlowControlServiceMeta;
import io.sermant.flowcontrol.common.factory.FlowControlThreadFactory;

import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * Flow control plugin public capability unified initialization entry
 *
 * @author zhouss
 * @since 2022-01-25
 */
public class FlowControlInitServiceImpl implements PluginService {
    private static final Logger LOGGER = LoggerFactory.getLogger();

    private final ThreadPoolExecutor executor = new ThreadPoolExecutor(1, 1, 0,
            TimeUnit.SECONDS, new SynchronousQueue<>(), new FlowControlThreadFactory("FLOW_CONTROL_INIT_THREAD"));

    private final FlowControlLifeCycle flowControlLifeCycle = new FlowControlLifeCycle();

    /**
     * Start the initialization task. This task is removed from the service life cycle and controlled through the
     * interception point to obtain accurate data
     */
    public void doStart() {
        executor.execute(flowControlLifeCycle);
    }

    @Override
    public void stop() {
        MatchManager.INSTANCE.getMatchedCache().release();
    }

    /**
     * Flow control initialization logic life cycle
     *
     * @since 2022-03-22
     */
    static class FlowControlLifeCycle implements Runnable {
        @Override
        public void run() {
            DynamicConfigService dynamicConfigService = getDynamicConfigService();
            if (dynamicConfigService == null) {
                LOGGER.severe("dynamicConfigService is null, fail to init FlowControlLifeCycle!");
                return;
            }

            ConfigSubscriber configSubscriber;
            final FlowControlConfig pluginConfig = PluginConfigManager.getPluginConfig(FlowControlConfig.class);
            if (pluginConfig.isUseCseRule()) {
                // Adapting to the cse starts to adapt the dedicated configuration listener of the cse
                configSubscriber = new CseGroupConfigSubscriber(FlowControlServiceMeta.getInstance().getServiceName(),
                        new RuleDynamicConfigListener(), dynamicConfigService, "FlowControl");
            } else {
                configSubscriber = new DefaultGroupConfigSubscriber(
                        FlowControlServiceMeta.getInstance().getServiceName(),
                        new RuleDynamicConfigListener(), dynamicConfigService,
                        "FlowControl");
            }
            configSubscriber.subscribe();
        }

        private DynamicConfigService getDynamicConfigService() {
            final FlowControlConfig pluginConfig = PluginConfigManager.getPluginConfig(FlowControlConfig.class);
            DynamicConfigService dynamicConfigService;

            try {
                // Determine whether to use the configuration center based on requirements
                if (pluginConfig.isUseAgentConfigCenter()) {
                    dynamicConfigService = ServiceManager.getService(DynamicConfigService.class);
                } else {
                    dynamicConfigService = OperationManager.getOperation(DynamicConfigServiceInitializer.class)
                            .initKieDynamicConfigService(pluginConfig.getConfigKieAddress(), pluginConfig.getProject());
                }
            } catch (IllegalArgumentException e) {
                LOGGER.severe("dynamicConfigService is not enabled!");
                dynamicConfigService = null;
            }

            return dynamicConfigService;
        }
    }
}
