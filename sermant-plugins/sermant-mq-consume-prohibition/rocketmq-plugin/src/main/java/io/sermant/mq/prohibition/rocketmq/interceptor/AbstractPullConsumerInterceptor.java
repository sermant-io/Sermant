/*
 *  Copyright (C) 2023-2023 Huawei Technologies Co., Ltd. All rights reserved.
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

package io.sermant.mq.prohibition.rocketmq.interceptor;

import io.sermant.core.plugin.agent.interceptor.AbstractInterceptor;
import io.sermant.mq.prohibition.controller.config.ProhibitionConfigManager;
import io.sermant.mq.prohibition.controller.rocketmq.RocketMqPullConsumerController;
import io.sermant.mq.prohibition.controller.rocketmq.extension.RocketMqConsumerHandler;
import io.sermant.mq.prohibition.controller.rocketmq.wrapper.DefaultLitePullConsumerWrapper;

import org.apache.rocketmq.common.message.MessageQueue;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * PullConsumer abstract interceptor
 *
 * @author daizhenyu
 * @since 2023-12-04
 **/
public abstract class AbstractPullConsumerInterceptor extends AbstractInterceptor {
    /**
     * External extension handler
     */
    protected RocketMqConsumerHandler handler;

    /**
     * Non-parametric construction method
     */
    public AbstractPullConsumerInterceptor() {
    }

    /**
     * Parameterized construction method
     *
     * @param handler External extension handler
     */
    public AbstractPullConsumerInterceptor(RocketMqConsumerHandler handler) {
        this.handler = handler;
    }

    /**
     * PullConsumer performs prohibited consumption operations
     *
     * @param pullConsumerWrapper pullConsumer packaging class instance
     */
    protected void disablePullConsumption(DefaultLitePullConsumerWrapper pullConsumerWrapper) {
        if (pullConsumerWrapper != null) {
            RocketMqPullConsumerController.disablePullConsumption(pullConsumerWrapper,
                    ProhibitionConfigManager.getRocketMqProhibitionTopics());
        }
    }

    /**
     * Get the topic of the message queue
     *
     * @param messageQueues Message queue
     * @return Topic of message queue
     */
    protected Set<String> getMessageQueueTopics(Collection<MessageQueue> messageQueues) {
        HashSet<String> topics = new HashSet<>();
        for (MessageQueue messageQueue : messageQueues) {
            topics.add(messageQueue.getTopic());
        }
        return topics;
    }
}
