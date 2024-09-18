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

package io.sermant.mq.grayscale.rocketmq.interceptor;

import io.sermant.core.plugin.agent.entity.ExecuteContext;

import org.apache.rocketmq.client.consumer.DefaultMQPullConsumer;
import org.apache.rocketmq.client.impl.consumer.DefaultMQPullConsumerImpl;

/**
 * PullConsumer fetchSubscribeMessageQueues/fetchMessageQueuesInBalance method interceptor
 * base scene recording namesrvAddr、topic、group info
 *
 * @author chengyouling
 * @since 2024-05-27
 **/
public class RocketMqPullConsumerFetchInterceptor extends RocketMqAbstractInterceptor {
    @Override
    public ExecuteContext doAfter(ExecuteContext context) throws Exception {
        DefaultMQPullConsumer pullConsumer =
                ((DefaultMQPullConsumerImpl) context.getObject()).getDefaultMQPullConsumer();
        buildGroupAndClientConfig(pullConsumer.getNamesrvAddr(), (String) context.getArguments()[0],
                pullConsumer.getConsumerGroup());
        return context;
    }
}
