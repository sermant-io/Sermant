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

package com.huawei.flowcontrol.common.init;

import com.huawei.flowcontrol.common.config.FlowControlConfig;
import com.huawei.flowcontrol.common.core.match.MatchManager;
import com.huawei.flowcontrol.common.core.rule.RuleDynamicConfigListener;
import com.huawei.flowcontrol.common.entity.FlowControlServiceMeta;
import com.huawei.flowcontrol.common.factory.FlowControlThreadFactory;

import com.huaweicloud.sermant.core.common.LoggerFactory;
import com.huaweicloud.sermant.core.operation.OperationManager;
import com.huaweicloud.sermant.core.operation.initializer.DynamicConfigServiceInitializer;
import com.huaweicloud.sermant.core.plugin.config.PluginConfigManager;
import com.huaweicloud.sermant.core.plugin.service.PluginService;
import com.huaweicloud.sermant.core.plugin.subscribe.ConfigSubscriber;
import com.huaweicloud.sermant.core.plugin.subscribe.CseGroupConfigSubscriber;
import com.huaweicloud.sermant.core.plugin.subscribe.DefaultGroupConfigSubscriber;
import com.huaweicloud.sermant.core.service.ServiceManager;
import com.huaweicloud.sermant.core.service.dynamicconfig.DynamicConfigService;

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
