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
 * 拦截器开关抽象类
 *
 * @author zhp
 * @since 2023-02-21
 */
public abstract class AbstractSwitchInterceptor extends AbstractInterceptor {
    /**
     * 离群实例摘除配置
     */
    protected static final RemovalConfig REMOVAL_CONFIG = PluginConfigManager.getConfig(RemovalConfig.class);

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
     * 前置触发点
     *
     * @param context 执行上下文
     * @return 执行上下文
     */
    protected abstract ExecuteContext doBefore(ExecuteContext context);

    /**
     * 后置触发点
     *
     * @param context 执行上下文
     * @return 执行上下文
     */
    protected abstract ExecuteContext doAfter(ExecuteContext context);
}
