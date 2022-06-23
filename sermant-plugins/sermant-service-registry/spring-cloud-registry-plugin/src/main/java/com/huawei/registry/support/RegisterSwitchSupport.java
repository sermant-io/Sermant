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
 * 注册开关控制
 *
 * @author zhouss
 * @since 2022-03-02
 */
public abstract class RegisterSwitchSupport implements Interceptor {
    protected final RegisterConfig registerConfig;

    /**
     * 注册开关构造器
     */
    public RegisterSwitchSupport() {
        registerConfig = PluginConfigManager.getPluginConfig(RegisterConfig.class);
    }

    /**
     * 判断是否符合开启条件
     *
     * @return 判断是否符合开启条件
     */
    protected boolean isEnabled() {
        return registerConfig.isEnableSpringRegister() && registerConfig.isOpenMigration();
    }

    /**
     * 子类实现，默认为配置的注册开关 若需修改，则需重新实现该方法 满足条件:
     * <li>已开启spring注册</li>
     * <li>配置中心已下发关闭注册中心指令或者属于单注册的场景</li>
     *
     * @return 是否可关闭注册中心
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
