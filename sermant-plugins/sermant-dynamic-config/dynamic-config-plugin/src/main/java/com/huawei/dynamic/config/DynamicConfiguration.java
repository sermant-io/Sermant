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

import com.huaweicloud.sermant.core.config.common.ConfigTypeKey;
import com.huaweicloud.sermant.core.plugin.config.PluginConfig;

import java.time.Duration;

/**
 * 动态配置插件配置类
 *
 * @author zhouss
 * @since 2022-04-13
 */
@ConfigTypeKey("dynamic.config.plugin")
public class DynamicConfiguration implements PluginConfig {
    /**
     * 是否开启CSE适配
     */
    private boolean enableCseAdapter = true;

    /**
     * 内容为yaml的配置项列表, 多个使用逗号分隔, 例如:sourceKey1, sourceKey2
     */
    private String sourceKeys;

    /**
     * 是否开启动态配置
     */
    private boolean enableDynamicConfig = false;

    /**
     * 初次刷新spring事件延迟, 防止初始化时多次调用多次刷新, 影响宿主
     */
    private long firstRefreshDelayMs = Duration.ofMinutes(1L).toMillis();

    /**
     * 是否屏蔽原生的Spring Cloud Config系的配置中心开关
     */
    private boolean enableOriginConfigCenter = true;

    public boolean isEnableOriginConfigCenter() {
        return enableOriginConfigCenter;
    }

    public void setEnableOriginConfigCenter(boolean enableOriginConfigCenter) {
        this.enableOriginConfigCenter = enableOriginConfigCenter;
    }

    public long getFirstRefreshDelayMs() {
        return firstRefreshDelayMs;
    }

    public void setFirstRefreshDelayMs(long firstRefreshDelayMs) {
        this.firstRefreshDelayMs = firstRefreshDelayMs;
    }

    public boolean isEnableCseAdapter() {
        return enableCseAdapter;
    }

    public void setEnableCseAdapter(boolean enableCseAdapter) {
        this.enableCseAdapter = enableCseAdapter;
    }

    public boolean isEnableDynamicConfig() {
        return enableDynamicConfig;
    }

    public void setEnableDynamicConfig(boolean enableDynamicConfig) {
        this.enableDynamicConfig = enableDynamicConfig;
    }

    public String getSourceKeys() {
        return sourceKeys;
    }

    public void setSourceKeys(String sourceKeys) {
        this.sourceKeys = sourceKeys;
    }
}
