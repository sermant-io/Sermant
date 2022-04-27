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

package com.huawei.dynamic.config;

import com.huaweicloud.sermant.core.common.LoggerFactory;
import com.huaweicloud.sermant.core.plugin.config.PluginConfigManager;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

/**
 * 配置刷新通知器
 *
 * @author zhouss
 * @since 2022-04-13
 */
public class RefreshNotifier {
    private static final Logger LOGGER = LoggerFactory.getLogger();

    private static final int LISTENER_INIT_SIZE = 4;

    private final List<RefreshEventListener> refreshEventListeners = new ArrayList<>(LISTENER_INIT_SIZE);

    private boolean isInitialized = false;

    /**
     * 添加监听数据
     *
     * @param listener 监听器
     */
    public void addListener(RefreshEventListener listener) {
        refreshEventListeners.add(listener);
    }

    /**
     * 通知刷新事件
     */
    public void refresh() {
        if (!isInitialized) {
            return;
        }
        for (RefreshEventListener listener : refreshEventListeners) {
            listener.refresh();
        }
    }

    /**
     * 初始化等待方法
     */
    public void init() {
        final long firstRefreshDelayMs = PluginConfigManager.getPluginConfig(DynamicConfiguration.class)
            .getFirstRefreshDelayMs();
        CompletableFuture.runAsync(() -> {
            try {
                Thread.sleep(firstRefreshDelayMs);
            } catch (InterruptedException ex) {
                LOGGER.info("[DynamicConfig] notify wait thread terminate!");
            }
        }).thenApply(unused -> {
            isInitialized = true;
            refresh();
            return unused;
        });
    }
}
