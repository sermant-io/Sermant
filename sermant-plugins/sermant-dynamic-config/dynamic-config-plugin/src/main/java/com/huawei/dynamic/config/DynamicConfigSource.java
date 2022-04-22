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

import com.huawei.sermant.core.plugin.config.PluginConfigManager;
import com.huawei.sermant.core.service.dynamicconfig.common.DynamicConfigEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * 动态配置
 *
 * @author zhouss
 * @since 2022-04-15
 */
public abstract class DynamicConfigSource implements ConfigSource {
    /**
     * 动态配置
     */
    protected final DynamicConfiguration configuration;

    /**
     * 指定配置键
     */
    private final List<String> sourceKeys = new ArrayList<>();

    /**
     * 动态配置源初始化, 解析配置源指定的配置键
     */
    protected DynamicConfigSource() {
        configuration = PluginConfigManager.getPluginConfig(DynamicConfiguration.class);
        if (configuration.getSourceKeys() != null) {
            final String[] sources = configuration.getSourceKeys().split(",");
            for (String key : sources) {
                sourceKeys.add(key.trim());
            }
        }
    }

    /**
     * 配置事件更新
     *
     * @param event 配置事件
     * @return 是否要求刷新配置
     */
    public final boolean accept(DynamicConfigEvent event) {
        // 配置读取条件 1、如果没有指定key则全部读取 2、如果指定了配置key则只读取指定的键
        if (sourceKeys.isEmpty() || sourceKeys.contains(event.getKey())) {
            return doAccept(event);
        }
        return false;
    }

    /**
     * 底层实现配置更新
     *
     * @param event 通知事件
     * @return 处理成功返回true
     */
    protected abstract boolean doAccept(DynamicConfigEvent event);
}
