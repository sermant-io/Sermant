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

import com.huawei.dynamic.config.DynamicContext;

import com.huaweicloud.sermant.core.plugin.agent.entity.ExecuteContext;

/**
 * 通过拦截开启启动配置后的调用方法来判断是否开启启动配置, 该逻辑将在{@link SpringEnvironmentInterceptor}逻辑之前执行
 *
 * @author zhouss
 * @since 2022-04-13
 */
public class BootstrapListenerInterceptor extends DynamicConfigSwitchSupport {
    @Override
    public ExecuteContext before(ExecuteContext context) {
        return context;
    }

    @Override
    public ExecuteContext doAfter(ExecuteContext context) {
        DynamicContext.INSTANCE.setEnableBootstrap(true);
        return context;
    }
}
