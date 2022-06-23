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
 * 注册中心健康状态变更
 *
 * @author zhouss
 * @since 2021-12-13
 */
public class NacosHealthInterceptor extends SingleStateCloseHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger();

    private boolean isValidResult(Object result) {
        // 低版本不可用则返回0L, 反之可用
        // 高版本不可用返回空，可用返回ObjectNode, json格式({"clientBeatInterval":5000,"code":10200,"lightBeatEnabled":true}),
        // 判断字段clientBeatInterval是否大于0L
        return result == null || result instanceof Long || result instanceof ObjectNode;
    }

    @Override
    protected void close() {
        // 关闭nacos心跳发送
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
                // 新版本
                ObjectNode node = (ObjectNode) result;
                beat = node.get("clientBeatInterval").asLong();
            } else if (result instanceof Long) {
                // 旧版本
                beat = (Long) result;
            } else {
                return context;
            }

            // 如果心跳为0L，则当前实例与nacos注册中心不通，针对该实例注册中心已失效
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
