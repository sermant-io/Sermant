/*
 * Copyright (C) 2021-2022 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huawei.register.interceptors.health;

import com.huawei.register.context.RegisterContext;
import com.huawei.register.handler.SingleStateCloseHandler;
import com.huawei.sermant.core.common.LoggerFactory;
import com.huawei.sermant.core.plugin.agent.entity.ExecuteContext;

import java.util.logging.Logger;

/**
 * 注册中心健康状态变更
 *
 * @author zhouss
 * @since 2021-12-13
 */
public class EurekaHealthInterceptor extends SingleStateCloseHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger();

    @Override
    protected boolean needCloseRegisterCenter() {
        return super.needCloseRegisterCenter() && super.target != null;
    }

    @Override
    protected void close() throws Exception {
        // 关闭Eureka定时器
        final Class<?> discoveryClientClass = Thread.currentThread().getContextClassLoader()
            .loadClass("com.netflix.discovery.DiscoveryClient");
        discoveryClientClass.getDeclaredMethod("shutdown").invoke(target);
        LOGGER.info("Eureka register center has been closed.");
    }

    @Override
    public ExecuteContext doBefore(ExecuteContext context) {
        setArguments(context.getArguments());
        setTarget(context.getObject());
        return context;
    }

    @Override
    public ExecuteContext doAfter(ExecuteContext context) {
        final Object result = context.getResult();
        if (result instanceof Boolean) {
            final boolean heartbeatResult = (Boolean) result;
            if (heartbeatResult && RegisterContext.INSTANCE.compareAndSet(false, true)) {
                doChange(context.getObject(), arguments, false, true);
            }
            if (!heartbeatResult && RegisterContext.INSTANCE.compareAndSet(true, false)) {
                doChange(context.getObject(), arguments, true, false);
            }
        }
        return context;
    }
}
