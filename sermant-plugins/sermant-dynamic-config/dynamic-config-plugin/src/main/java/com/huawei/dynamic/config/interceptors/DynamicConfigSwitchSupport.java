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

package com.huawei.dynamic.config.interceptors;

import com.huawei.dynamic.config.ConfigHolder;
import com.huawei.dynamic.config.DynamicConfiguration;
import com.huawei.dynamic.config.source.OriginConfigDisableSource;

import com.huaweicloud.sermant.core.plugin.agent.entity.ExecuteContext;
import com.huaweicloud.sermant.core.plugin.agent.interceptor.Interceptor;
import com.huaweicloud.sermant.core.plugin.config.PluginConfigManager;

/**
 * 动态配置开关控制
 *
 * @author zhouss
 * @since 2022-04-14
 */
public class DynamicConfigSwitchSupport implements Interceptor {
    private final DynamicConfiguration dynamicConfiguration;

    /**
     * 注册开关构造器
     */
    public DynamicConfigSwitchSupport() {
        dynamicConfiguration = PluginConfigManager.getPluginConfig(DynamicConfiguration.class);
    }

    /**
     * 原配置中心是否已下发动态关闭
     *
     * @return 是否关闭
     */
    protected boolean isDynamicClosed() {
        final Object config = ConfigHolder.INSTANCE.getConfig(OriginConfigDisableSource.ZK_CONFIG_CENTER_ENABLED);
        if (config == null) {
            return false;
        }
        return !Boolean.parseBoolean(config.toString());
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

    private boolean isEnabled() {
        return dynamicConfiguration.isEnableDynamicConfig();
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
