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

package com.huawei.register.support;

import com.huawei.register.config.RegisterConfig;
import com.huawei.sermant.core.plugin.agent.entity.ExecuteContext;
import com.huawei.sermant.core.plugin.agent.interceptor.Interceptor;
import com.huawei.sermant.core.plugin.config.PluginConfigManager;

/**
 * 注册开关控制
 *
 * @author zhouss
 * @since 2022-03-02
 */
public abstract class RegisterSwitchSupport implements Interceptor {
    private final RegisterConfig registerConfig;

    public RegisterSwitchSupport() {
        registerConfig = PluginConfigManager.getPluginConfig(RegisterConfig.class);
    }

    /**
     * 判断是否开启spring注册
     *
     * @return 是否开启spring注册
     */
    protected final boolean isEnableSpringRegister() {
        return registerConfig.isEnableSpringRegister();
    }

    @Override
    public ExecuteContext before(ExecuteContext context) {
        if (isEnableSpringRegister()) {
            return doBefore(context);
        }
        return context;
    }

    @Override
    public ExecuteContext after(ExecuteContext context) {
        if (isEnableSpringRegister()) {
            return doAfter(context);
        }
        return context;
    }

    @Override
    public ExecuteContext onThrow(ExecuteContext context) {
        if (isEnableSpringRegister()) {
            return doThrow(context);
        }
        return context;
    }

    /**
     * 前置触发点
     *
     * @param context 执行上下文
     * @return 执行上下文
     */
    protected ExecuteContext doBefore(ExecuteContext context) {
        return context;
    }

    /**
     * 后置触发点
     *
     * @param context 执行上下文
     * @return 执行上下文
     */
    protected ExecuteContext doAfter(ExecuteContext context) {
        return context;
    }

    /**
     * 异常触发点
     *
     * @param context 执行上下文
     * @return 执行上下文
     */
    protected ExecuteContext doThrow(ExecuteContext context) {
        return context;
    }
}
