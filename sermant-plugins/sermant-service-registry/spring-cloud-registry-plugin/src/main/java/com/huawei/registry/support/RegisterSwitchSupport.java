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

package com.huawei.registry.support;

import com.huawei.registry.config.RegisterConfig;
import com.huawei.registry.config.RegisterDynamicConfig;

import com.huaweicloud.sermant.core.plugin.agent.entity.ExecuteContext;
import com.huaweicloud.sermant.core.plugin.agent.interceptor.Interceptor;
import com.huaweicloud.sermant.core.plugin.config.PluginConfigManager;

/**
 * Register switch controls
 *
 * @author zhouss
 * @since 2022-03-02
 */
public abstract class RegisterSwitchSupport implements Interceptor {
    /**
     * register configuration class
     */
    protected final RegisterConfig registerConfig;

    /**
     * Register the switch constructor
     */
    public RegisterSwitchSupport() {
        registerConfig = PluginConfigManager.getPluginConfig(RegisterConfig.class);
    }

    /**
     * Check whether the conditions are met
     *
     * @return Check whether the conditions are met
     */
    protected boolean isEnabled() {
        return registerConfig.isEnableSpringRegister() && registerConfig.isOpenMigration();
    }

    /**
     * Subclass implementation, default is the configured registration switch If you need to modify it, you need to
     * re-implement the method Condition met:
     * <li>Spring registration has been enabled</li>
     * <li>The configuration center has issued a command to close the registry or is a single registration</li>
     *
     * @return Whether the registry can be closed
     */
    protected boolean needCloseRegisterCenter() {
        return (RegisterDynamicConfig.INSTANCE.isNeedCloseOriginRegisterCenter() || !registerConfig.isOpenMigration())
                && isEnabled();
    }

    @Override
    public ExecuteContext before(ExecuteContext context) {
        if (isEnabled()) {
            return doBefore(context);
        }
        return context;
    }

    @Override
    public ExecuteContext after(ExecuteContext context) {
        if (isEnabled()) {
            return doAfter(context);
        }
        return context;
    }

    @Override
    public ExecuteContext onThrow(ExecuteContext context) {
        if (isEnabled()) {
            return doThrow(context);
        }
        return context;
    }

    /**
     * Pre-trigger point
     *
     * @param context Execution context
     * @return Execution context
     */
    protected ExecuteContext doBefore(ExecuteContext context) {
        return context;
    }

    /**
     * Rear trigger point
     *
     * @param context Execution context
     * @return Execution context
     */
    protected ExecuteContext doAfter(ExecuteContext context) {
        return context;
    }

    /**
     * Exception trigger point
     *
     * @param context Execution context
     * @return Execution context
     */
    protected ExecuteContext doThrow(ExecuteContext context) {
        return context;
    }
}
