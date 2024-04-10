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

package com.huawei.registry.interceptors.health;

import com.huawei.registry.context.RegisterContext;
import com.huawei.registry.handler.SingleStateCloseHandler;

import com.huaweicloud.sermant.core.common.LoggerFactory;
import com.huaweicloud.sermant.core.plugin.agent.entity.ExecuteContext;

import com.alibaba.nacos.client.naming.beat.BeatInfo;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.logging.Logger;

/**
 * Registry health status change, for Nacos1.x, Nacos2.x http protocol
 *
 * @author zhouss
 * @since 2021-12-13
 */
public class NacosHealthInterceptor extends SingleStateCloseHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger();

    private boolean isValidResult(Object result) {
        // If the lower version is not available, it will return 0L, otherwise it will be available
        // If the higher version is not available, it will return empty, but it can return ObjectNode,
        // json format({"clientBeatInterval":5000,"code":10200,"lightBeatEnabled":true}),
        // Check if the clientBeatInterval in the field is greater than 0L
        return result == null || result instanceof Long || result instanceof ObjectNode;
    }

    @Override
    protected void close() {
        // Turn off nacos heartbeat transmission
        BeatInfo beatInfo = (BeatInfo) arguments[0];
        beatInfo.setStopped(true);
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
        if (isValidResult(result)) {
            if (result == null) {
                RegisterContext.INSTANCE.compareAndSet(true, false);
                return context;
            }
            long beat;
            if (result instanceof ObjectNode) {
                // New version
                ObjectNode node = (ObjectNode) result;
                beat = node.get("clientBeatInterval").asLong();
            } else if (result instanceof Long) {
                // Older versions
                beat = (Long) result;
            } else {
                return context;
            }

            // If the heartbeat is 0 L,
            // the current instance cannot communicate with the Nacos registry
            // and the registry for the instance is invalid
            if (beat == 0L) {
                RegisterContext.INSTANCE.compareAndSet(true, false);
            } else if (beat > 0L) {
                RegisterContext.INSTANCE.compareAndSet(false, true);
            } else {
                return context;
            }
        }
        return context;
    }
}
