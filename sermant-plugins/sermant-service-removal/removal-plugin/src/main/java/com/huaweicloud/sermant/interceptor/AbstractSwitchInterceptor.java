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

package com.huaweicloud.sermant.interceptor;

import com.huaweicloud.sermant.config.RemovalConfig;
import com.huaweicloud.sermant.core.plugin.agent.entity.ExecuteContext;
import com.huaweicloud.sermant.core.plugin.agent.interceptor.AbstractInterceptor;
import com.huaweicloud.sermant.core.plugin.config.PluginConfigManager;

/**
 * Interceptor switch abstract class
 *
 * @author zhp
 * @since 2023-02-21
 */
public abstract class AbstractSwitchInterceptor extends AbstractInterceptor {
    /**
     * Outlier instance removal configuration
     */
    protected static final RemovalConfig REMOVAL_CONFIG = PluginConfigManager.getPluginConfig(RemovalConfig.class);

    @Override
    public ExecuteContext before(ExecuteContext context) {
        if (!REMOVAL_CONFIG.isEnableRemoval()) {
            return context;
        }
        return this.doBefore(context);
    }

    @Override
    public ExecuteContext after(ExecuteContext context) {
        if (!REMOVAL_CONFIG.isEnableRemoval()) {
            return context;
        }
        return this.doAfter(context);
    }

    /**
     * Pre-trigger point
     *
     * @param context Execution context
     * @return Execution context
     */
    protected abstract ExecuteContext doBefore(ExecuteContext context);

    /**
     * Rear trigger point
     *
     * @param context Execution context
     * @return Execution context
     */
    protected abstract ExecuteContext doAfter(ExecuteContext context);
}
