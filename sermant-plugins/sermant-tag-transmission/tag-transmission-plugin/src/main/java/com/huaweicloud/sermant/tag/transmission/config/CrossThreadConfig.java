/*
 * Copyright (C) 2023-2023 Huawei Technologies Co., Ltd. All rights reserved.
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
 * 跨线程传递开关配置
 *
 * @author lilai
 * @since 2023-08-02
 */
@ConfigTypeKey("crossthread.config")
public class CrossThreadConfig implements PluginConfig {
    /**
     * 是否在非定时线程池中传递标签
     */
    @ConfigFieldKey("enabled-thread-pool")
    private boolean enabledThreadPool;

    /**
     * 是否在定时线程池的schedule/scheduleAtFixedRate/scheduleWithFixedDelay方法中传递标签
     */
    @ConfigFieldKey("enabled-scheduler")
    private boolean enabledScheduler;

    /**
     * 是否在直接new Thread时传递标签
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
