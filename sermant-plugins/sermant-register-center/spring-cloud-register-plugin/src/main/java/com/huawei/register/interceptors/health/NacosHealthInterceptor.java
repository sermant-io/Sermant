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

import com.alibaba.nacos.client.naming.beat.BeatInfo;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.huawei.register.context.RegisterContext;
import com.huawei.register.handler.SingleStateCloseHandler;
import com.huawei.sermant.core.agent.common.BeforeResult;
import com.huawei.sermant.core.agent.interceptor.InstanceMethodInterceptor;
import com.huawei.sermant.core.common.LoggerFactory;

import java.lang.reflect.Method;
import java.util.Locale;
import java.util.logging.Logger;

/**
 * 注册中心健康状态变更
 *
 * @author zhouss
 * @since 2021-12-13
 */
public class NacosHealthInterceptor extends SingleStateCloseHandler implements InstanceMethodInterceptor {
    private static final Logger LOGGER = LoggerFactory.getLogger();

    @Override
    public void before(Object obj, Method method, Object[] arguments, BeforeResult beforeResult) {
        setArguments(arguments);
        setTarget(obj);
    }

    @Override
    public Object after(Object obj, Method method, Object[] arguments, Object result) {
        if (isValidResult(result)) {
            if (result == null && RegisterContext.INSTANCE.compareAndSet(true, false)) {
                doChange(obj, arguments, true, false);
                return null;
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
                return result;
            }
            // 如果心跳为0L，则当前实例与nacos注册中心不通，针对该实例注册中心已失效
            if (beat == 0L && RegisterContext.INSTANCE.compareAndSet(true, false)) {
                doChange(obj, arguments, true, false);
            } else if (beat > 0L && RegisterContext.INSTANCE.compareAndSet(false, true)) {
                doChange(obj, arguments, false, true);
            } else {
                return result;
            }
        }
        return result;
    }

    private boolean isValidResult(Object result) {
        // 低版本不可用则返回0L, 反之可用
        // 高版本不可用返回空，可用返回ObjectNode, json格式({"clientBeatInterval":5000,"code":10200,"lightBeatEnabled":true}),
        // 判断字段clientBeatInterval是否大于0L
        return result == null || result instanceof Long || result instanceof ObjectNode;
    }

    @Override
    public void onThrow(Object obj, Method method, Object[] arguments, Throwable t) {
    }

    @Override
    protected void close() {
        // 关闭nacos心跳发送
        BeatInfo beatInfo = (BeatInfo) arguments[0];
        beatInfo.setStopped(true);
        LOGGER.info("Nacos heartbeat has been closed.");
    }
}
