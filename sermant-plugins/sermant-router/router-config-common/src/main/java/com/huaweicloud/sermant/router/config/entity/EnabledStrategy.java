/*
 * Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.huaweicloud.sermant.router.config.entity;

import com.huaweicloud.sermant.core.plugin.config.PluginConfigManager;
import com.huaweicloud.sermant.router.common.config.RouterConfig;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 生效策略
 *
 * @author provenceee
 * @since 2022-10-08
 */
public class EnabledStrategy {
    /**
     * 策略值
     */
    private final List<String> value = new CopyOnWriteArrayList<>();

    /**
     * 默认策略
     */
    private final Strategy defaultStrategy;

    /**
     * 开启策略
     */
    private Strategy strategy;

    /**
     * 构造方法
     */
    public EnabledStrategy() {
        RouterConfig routerConfig = PluginConfigManager.getPluginConfig(RouterConfig.class);
        defaultStrategy = Strategy.valueOf(routerConfig.getZoneRouterDefaultStrategy().toUpperCase(Locale.ROOT));
        strategy = defaultStrategy;
    }

    public Strategy getStrategy() {
        return strategy;
    }

    public List<String> getValue() {
        return Collections.unmodifiableList(value);
    }

    /**
     * 重置配置
     */
    public void reset() {
        reset(defaultStrategy, Collections.emptyList());
    }

    /**
     * 重置配置
     *
     * @param newStrategy 开启策略
     * @param newValue 策略值
     */
    public void reset(Strategy newStrategy, List<String> newValue) {
        this.strategy = newStrategy;
        this.value.clear();
        this.value.addAll(newValue);
    }
}