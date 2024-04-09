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

package com.huawei.dynamic.config;

import com.huawei.dynamic.config.init.DynamicConfigThreadFactory;

import com.huaweicloud.sermant.core.common.LoggerFactory;
import com.huaweicloud.sermant.core.service.dynamicconfig.common.DynamicConfigEvent;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * Configuration holding, storage dynamic configuration
 *
 * @author zhouss
 * @since 2022-04-08
 */
public enum ConfigHolder {
    /**
     * singleton
     */
    INSTANCE;

    private static final Logger LOGGER = LoggerFactory.getLogger();

    private static final int QUEUE_SIZE = 10;

    private final RefreshNotifier notifier = new RefreshNotifier();

    private final List<ConfigSource> configSources = new LinkedList<>();

    private final ExecutorService executorService = new ThreadPoolExecutor(1, 1, 0,
            TimeUnit.MILLISECONDS, new ArrayBlockingQueue<>(QUEUE_SIZE), new DynamicConfigThreadFactory(
            "DYNAMIC_CONFIG_REFRESH_THREAD"));

    ConfigHolder() {
        loadConfigSources();
    }

    private void loadConfigSources() {
        for (ConfigSource configSource : ServiceLoader.load(ConfigSource.class, this.getClass().getClassLoader())) {
            if (configSource.isEnabled()) {
                configSources.add(configSource);
            }
        }
        Collections.sort(configSources);
    }

    /**
     * Parse the event to determine if you need to be notified to update the configuration
     * <p>full renewal</p>
     *
     * @param event event
     */
    public void resolve(DynamicConfigEvent event) {
        executorService.submit(() -> {
            boolean isNeedRefresh = false;
            for (ConfigSource configSource : configSources) {
                isNeedRefresh |= doAccept(configSource, event);
            }
            if (isNeedRefresh) {
                notifier.refresh(event);
            }
        });
    }

    private boolean doAccept(ConfigSource configSource, DynamicConfigEvent event) {
        return (configSource instanceof DynamicConfigSource)
                && ((DynamicConfigSource) configSource).accept(event);
    }

    /**
     * addListener
     *
     * @param listener listener
     */
    public void addListener(DynamicConfigListener listener) {
        if (listener == null) {
            LOGGER.warning("Dynamic Config listener can not be null!");
            return;
        }
        notifier.addListener(listener);
    }

    /**
     * get a single configuration
     *
     * @param key configuration key
     * @return configuration value
     */
    public Object getConfig(String key) {
        for (ConfigSource configSource : configSources) {
            final Object config = configSource.getConfig(key);
            if (config != null) {
                return config;
            }
        }
        return null;
    }

    /**
     * gets all configuration names
     *
     * @return all configuration names
     */
    public Set<String> getConfigNames() {
        final Set<String> configNames = new HashSet<>();
        for (ConfigSource configSource : configSources) {
            configNames.addAll(configSource.getConfigNames());
        }
        return configNames;
    }

    /**
     * gets all configuration sources
     *
     * @return configurationSource
     */
    public List<ConfigSource> getConfigSources() {
        return configSources;
    }
}
