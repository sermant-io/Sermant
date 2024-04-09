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

package com.huawei.flowcontrol.config;

import com.huawei.flowcontrol.common.init.FlowControlInitServiceImpl;

import com.huaweicloud.sermant.core.plugin.agent.entity.ExecuteContext;
import com.huaweicloud.sermant.core.plugin.agent.interceptor.AbstractInterceptor;
import com.huaweicloud.sermant.core.plugin.service.PluginServiceManager;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * End Phase Starts to initialize flow control configuration listening
 *
 * @author zhouss
 * @since 2022-01-28
 */
public class SpringApplicationInterceptor extends AbstractInterceptor {
    private static final AtomicBoolean INIT = new AtomicBoolean();

    @Override
    public ExecuteContext before(ExecuteContext context) throws Exception {
        return context;
    }

    @Override
    public ExecuteContext after(ExecuteContext context) {
        Object logStartupInfo = context.getMemberFieldValue("logStartupInfo");
        if ((logStartupInfo instanceof Boolean) && (Boolean) logStartupInfo && INIT.compareAndSet(false, true)) {
            final FlowControlInitServiceImpl service = PluginServiceManager.getPluginService(
                    FlowControlInitServiceImpl.class);
            service.doStart();
        }
        return context;
    }
}