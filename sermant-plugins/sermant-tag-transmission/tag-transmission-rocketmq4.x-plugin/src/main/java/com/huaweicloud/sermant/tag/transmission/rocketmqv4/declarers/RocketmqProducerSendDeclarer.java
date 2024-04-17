/*
 * Copyright (C) 2023-2023 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huaweicloud.sermant.tag.transmission.rocketmqv4.declarers;

import com.huaweicloud.sermant.core.plugin.agent.declarer.AbstractPluginDeclarer;
import com.huaweicloud.sermant.core.plugin.agent.declarer.InterceptDeclarer;
import com.huaweicloud.sermant.core.plugin.agent.matcher.ClassMatcher;
import com.huaweicloud.sermant.core.plugin.agent.matcher.MethodMatcher;
import com.huaweicloud.sermant.tag.transmission.rocketmqv4.interceptors.RocketmqProducerSendInterceptor;

/**
 * RocketMQ enhanced producer declarer for Traffic Label Transmission，supports RocketMQ4.x
 *
 * @author tangle
 * @since 2023-07-20
 */
public class RocketmqProducerSendDeclarer extends AbstractPluginDeclarer {
    private static final String ENHANCE_CLASS = "org.apache.rocketmq.client.impl.MQClientAPIImpl";

    private static final String METHOD_NAME = "sendMessage";

    private static final String[] METHOD_PARAM_TYPES = {
            "java.lang.String",
            "java.lang.String",
            "org.apache.rocketmq.common.message.Message",
            "org.apache.rocketmq.common.protocol.header.SendMessageRequestHeader",
            "long",
            "org.apache.rocketmq.client.impl.CommunicationMode",
            "org.apache.rocketmq.client.producer.SendCallback",
            "org.apache.rocketmq.client.impl.producer.TopicPublishInfo",
            "org.apache.rocketmq.client.impl.factory.MQClientInstance",
            "int",
            "org.apache.rocketmq.client.hook.SendMessageContext",
            "org.apache.rocketmq.client.impl.producer.DefaultMQProducerImpl"
    };

    @Override
    public ClassMatcher getClassMatcher() {
        return ClassMatcher.nameEquals(ENHANCE_CLASS);
    }

    @Override
    public InterceptDeclarer[] getInterceptDeclarers(ClassLoader classLoader) {
        return new InterceptDeclarer[]{
                InterceptDeclarer.build(MethodMatcher.nameEquals(METHOD_NAME)
                        .and(MethodMatcher.paramTypesEqual(METHOD_PARAM_TYPES)), new RocketmqProducerSendInterceptor())
        };
    }
}
