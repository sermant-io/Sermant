/*
 * Copyright (C) 2024-2024 Sermant Authors. All rights reserved.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package io.sermant.mq.grayscale.interceptor;

import io.sermant.core.plugin.agent.entity.ExecuteContext;
import io.sermant.core.plugin.agent.interceptor.AbstractInterceptor;
import io.sermant.core.utils.ReflectUtils;
import io.sermant.mq.grayscale.service.MqGraySendMessageHook;
import io.sermant.mq.grayscale.utils.MqGrayscaleConfigUtils;

import org.apache.rocketmq.client.hook.SendMessageHook;
import org.apache.rocketmq.client.impl.producer.DefaultMQProducerImpl;

import java.lang.reflect.Method;
import java.util.Optional;

/**
 * SendMessageHook builder interceptor
 *
 * @author chengyouling
 * @since 2024-05-27
 **/
public class RocketMqProducerGrayMessageHookInterceptor extends AbstractInterceptor {
    @Override
    public ExecuteContext before(ExecuteContext context) throws Exception {
        return context;
    }

    @Override
    public ExecuteContext after(ExecuteContext context) throws Exception {
        if (MqGrayscaleConfigUtils.isPlugEnabled()) {
            DefaultMQProducerImpl producer = (DefaultMQProducerImpl) context.getObject();
            Optional<Method> method = ReflectUtils.findMethod(producer.getClass(), "registerSendMessageHook",
                    new Class[]{SendMessageHook.class});
            if (method.isPresent()) {
                method.get().invoke(producer, new MqGraySendMessageHook());
            }
        }
        return context;
    }
}
