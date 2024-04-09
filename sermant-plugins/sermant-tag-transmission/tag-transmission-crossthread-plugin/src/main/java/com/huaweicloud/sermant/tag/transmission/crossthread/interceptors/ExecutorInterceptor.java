/*
 *   Copyright (C) 2023-2024 Sermant Authors. All rights reserved.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package com.huaweicloud.sermant.tag.transmission.crossthread.interceptors;

import com.huaweicloud.sermant.core.plugin.config.PluginConfigManager;
import com.huaweicloud.sermant.tag.transmission.config.CrossThreadConfig;

/**
 * ExecutorInterceptor
 *
 * @author provenceee
 * @since 2023-04-21
 */
public class ExecutorInterceptor extends AbstractExecutorInterceptor {
    /**
     * construction method
     */
    public ExecutorInterceptor() {
        super(!PluginConfigManager.getPluginConfig(CrossThreadConfig.class).isEnabledThreadPool());
    }
}