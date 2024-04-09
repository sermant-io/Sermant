/*
 * Copyright (C) 2023-2024 Sermant Authors. All rights reserved.
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

package com.huaweicloud.sermant.tag.transmission.config;

import com.huaweicloud.sermant.core.config.common.ConfigFieldKey;
import com.huaweicloud.sermant.core.config.common.ConfigTypeKey;
import com.huaweicloud.sermant.core.plugin.config.PluginConfig;

/**
 * switch configuration transmitted across threads
 *
 * @author lilai
 * @since 2023-08-02
 */
@ConfigTypeKey("crossthread.config")
public class CrossThreadConfig implements PluginConfig {
    /**
     * whether to transmit a tag in a non timed thread pool
     */
    @ConfigFieldKey("enabled-thread-pool")
    private boolean enabledThreadPool;

    /**
     * Whether to transmit the tag in the schedule/scheduleAtFixedRate/scheduleWithFixedDelay
     * method of the scheduled thread pool
     */
    @ConfigFieldKey("enabled-scheduler")
    private boolean enabledScheduler;

    /**
     * whether to transmit the tag when directly new thread
     */
    @ConfigFieldKey("enabled-thread")
    private boolean enabledThread;

    public boolean isEnabledThread() {
        return enabledThread;
    }

    public void setEnabledThread(boolean enabledThread) {
        this.enabledThread = enabledThread;
    }

    public boolean isEnabledThreadPool() {
        return enabledThreadPool;
    }

    public void setEnabledThreadPool(boolean enabledThreadPool) {
        this.enabledThreadPool = enabledThreadPool;
    }

    public boolean isEnabledScheduler() {
        return enabledScheduler;
    }

    public void setEnabledScheduler(boolean enabledScheduler) {
        this.enabledScheduler = enabledScheduler;
    }
}
