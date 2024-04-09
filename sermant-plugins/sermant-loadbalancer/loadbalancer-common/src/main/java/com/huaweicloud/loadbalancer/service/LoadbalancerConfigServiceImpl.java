/*
 * Copyright (C) 2022-2024 Sermant Authors. All rights reserved.
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

package com.huaweicloud.loadbalancer.service;

import com.huaweicloud.loadbalancer.config.LbContext;
import com.huaweicloud.loadbalancer.config.LoadbalancerConfig;
import com.huaweicloud.loadbalancer.factory.LoadbalancerThreadFactory;
import com.huaweicloud.loadbalancer.listener.LoadbalancerConfigListener;
import com.huaweicloud.sermant.core.plugin.config.PluginConfigManager;
import com.huaweicloud.sermant.core.plugin.service.PluginService;
import com.huaweicloud.sermant.core.plugin.subscribe.ConfigSubscriber;
import com.huaweicloud.sermant.core.plugin.subscribe.CseGroupConfigSubscriber;
import com.huaweicloud.sermant.core.plugin.subscribe.DefaultGroupConfigSubscriber;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * basic load balancing configuration class
 *
 * @author provenceee
 * @since 2022-01-22
 */
public class LoadbalancerConfigServiceImpl implements PluginService {
    private final ExecutorService executorService = Executors.newSingleThreadExecutor(
            new LoadbalancerThreadFactory("loadbalancer-plugin-init-thread"));

    private LoadbalancerConfig config;

    /**
     * initialization notification
     */
    @Override
    public void start() {
        config = PluginConfigManager.getPluginConfig(LoadbalancerConfig.class);
    }

    /**
     * subscription configuration
     */
    public void subscribe() {
        executorService.execute(this::doSubscribe);
    }

    private void doSubscribe() {
        ConfigSubscriber configSubscriber;
        String pluginName = "loadbalancer-plugin";
        if (config.isUseCseRule()) {
            configSubscriber = new CseGroupConfigSubscriber(LbContext.INSTANCE.getServiceName(),
                    new LoadbalancerConfigListener(), pluginName);
        } else {
            configSubscriber = new DefaultGroupConfigSubscriber(LbContext.INSTANCE.getServiceName(),
                    new LoadbalancerConfigListener(), pluginName);
        }
        configSubscriber.subscribe();
    }

    @Override
    public void stop() {
        executorService.shutdown();
    }
}
