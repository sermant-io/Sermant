/*
 * Copyright (C) 2022-2024 Sermant Authors. All rights reserved.
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

package com.huawei.registry.interceptors.health;

import com.huawei.registry.support.RegisterSwitchSupport;

import com.huaweicloud.sermant.core.common.LoggerFactory;
import com.huaweicloud.sermant.core.plugin.agent.entity.ExecuteContext;
import com.huaweicloud.sermant.core.utils.ReflectUtils;

import java.util.Optional;
import java.util.logging.Logger;

/**
 * Before calling anti registration, check the status of RpcClient, and the shutdown status will no longer be used for
 * anti registration; Closed by {@link NacosRpcClientHealthInterceptor}, automatically taken offline from the
 * registration center
 *
 * @author zhouss
 * @since 2022-12-20
 */
public class NacosGrpcDeRegisterInterceptor extends RegisterSwitchSupport {
    private static final Logger LOGGER = LoggerFactory.getLogger();

    @Override
    protected ExecuteContext doBefore(ExecuteContext context) {
        final Optional<Object> rpcClient = ReflectUtils.getFieldValue(context.getObject(), "rpcClient");
        if (!rpcClient.isPresent()) {
            return context;
        }
        final Object client = rpcClient.get();
        final Optional<Object> isShutdownRaw = ReflectUtils.invokeMethod(client, "isShutdown", null, null);
        if (isShutdownRaw.isPresent() && isShutdownRaw.get() instanceof Boolean) {
            boolean isShutdown = (boolean) isShutdownRaw.get();
            if (isShutdown) {
                LOGGER.info("RpcClient has been shutdown, skip deregister operation!");
                context.skip(null);
            }
        }
        return context;
    }
}
