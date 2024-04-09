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

import com.huaweicloud.sermant.core.config.common.ConfigTypeKey;
import com.huaweicloud.sermant.core.plugin.config.PluginConfig;

import java.time.Duration;

/**
 * Dynamically configure plugin configuration classes
 *
 * @author zhouss
 * @since 2022-04-13
 */
@ConfigTypeKey("dynamic.config.plugin")
public class DynamicConfiguration implements PluginConfig {
    /**
     * whether to enable cse adaptation
     */
    private boolean enableCseAdapter = true;

    /**
     * The content is a list of yaml configuration items, separated by commas, suchAs:sourceKey1, sourceKey2
     */
    private String sourceKeys;

    /**
     * whether to enable dynamic configuration
     */
    private boolean enableDynamicConfig = false;

    /**
     * The initial refresh spring event is delayed, preventing multiple calls to multiple refreshes during
     * initialization that affect the host
     */
    private long firstRefreshDelayMs = Duration.ofMinutes(1L).toMillis();

    /**
     * Whether to disable the native Spring Cloud Config configuration center switch
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
