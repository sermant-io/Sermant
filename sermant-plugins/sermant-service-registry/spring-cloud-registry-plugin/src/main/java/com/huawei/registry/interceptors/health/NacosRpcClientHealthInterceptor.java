/*
 * Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
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

import com.huawei.registry.context.RegisterContext;
import com.huawei.registry.handler.SingleStateCloseHandler;

import com.huaweicloud.sermant.core.common.LoggerFactory;
import com.huaweicloud.sermant.core.plugin.agent.entity.ExecuteContext;
import com.huaweicloud.sermant.core.utils.ReflectUtils;

import java.util.logging.Logger;

/**
 * 注册中心健康状态变更, 针对nacos2.x, Grpc协议
 *
 * @author zhouss
 * @since 2022-12-20
 */
public class NacosRpcClientHealthInterceptor extends SingleStateCloseHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger();

    @Override
    protected void close() {
        // 关闭nacos心跳发送
        ReflectUtils.invokeMethod(target, "shutdown", null, null);
        LOGGER.warning("Nacos heartbeat has been closed by user.");
    }

    @Override
    protected ExecuteContext doBefore(ExecuteContext context) {
        checkState(context, null);
        return context;
    }

    @Override
    public ExecuteContext doAfter(ExecuteContext context) {
        final Object result = context.getResult();
        if (result instanceof Boolean) {
            boolean health = (boolean) result;
            if (health) {
                RegisterContext.INSTANCE.compareAndSet(false, true);
                LOGGER.info("Nacos registry center recover healthy status!");
            } else {
                RegisterContext.INSTANCE.compareAndSet(true, false);
                LOGGER.info("Nacos registry center may be unhealthy!");
            }
        }
        return context;
    }
}
