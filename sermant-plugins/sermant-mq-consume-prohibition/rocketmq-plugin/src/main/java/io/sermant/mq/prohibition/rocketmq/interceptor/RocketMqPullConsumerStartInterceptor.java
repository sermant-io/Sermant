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

import io.sermant.core.plugin.agent.entity.ExecuteContext;
import io.sermant.mq.prohibition.controller.rocketmq.RocketMqPullConsumerController;
import io.sermant.mq.prohibition.controller.rocketmq.constant.SubscriptionType;
import io.sermant.mq.prohibition.controller.rocketmq.extension.RocketMqConsumerHandler;
import io.sermant.mq.prohibition.controller.rocketmq.wrapper.DefaultLitePullConsumerWrapper;
import io.sermant.mq.prohibition.rocketmq.utils.PullConsumerLocalInfoUtils;

import org.apache.rocketmq.client.consumer.DefaultLitePullConsumer;
import org.apache.rocketmq.common.message.MessageQueue;

import java.util.Collection;

/**
 * RocketMq pullConsumer Start Interceptor
 *
 * @author daizhenyu
 * @since 2023-12-04
 **/
public class RocketMqPullConsumerStartInterceptor extends AbstractPullConsumerInterceptor {
    /**
     * Non-parametric construction method
     */
    public RocketMqPullConsumerStartInterceptor() {
    }

    /**
     * Parameterized construction method
     *
     * @param handler Interception point handler
     */
    public RocketMqPullConsumerStartInterceptor(RocketMqConsumerHandler handler) {
        super(handler);
    }

    @Override
    public ExecuteContext before(ExecuteContext context) {
        if (handler != null) {
            handler.doBefore(context);
        }
        return context;
    }

    @Override
    public ExecuteContext after(ExecuteContext context) {
        DefaultLitePullConsumer pullConsumer = (DefaultLitePullConsumer) context.getObject();
        RocketMqPullConsumerController.cachePullConsumer(pullConsumer);

        // Get cached consumer packaging class instances
        DefaultLitePullConsumerWrapper pullConsumerWrapper =
                RocketMqPullConsumerController.getPullConsumerWrapper(pullConsumer);
        updatePushConsumerWrapperInfo(pullConsumerWrapper);

        if (handler != null) {
            handler.doAfter(context);
            return context;
        }

        // Consumer activation will execute consumption prohibition on consumers based on the cached consumption
        // prohibition configuration
        disablePullConsumption(pullConsumerWrapper);
        return context;
    }

    @Override
    public ExecuteContext onThrow(ExecuteContext context) {
        if (handler != null) {
            handler.doOnThrow(context);
        }
        return context;
    }

    private void updatePushConsumerWrapperInfo(DefaultLitePullConsumerWrapper pullConsumerWrapper) {
        if (pullConsumerWrapper != null) {
            pullConsumerWrapper.setSubscriptionType(PullConsumerLocalInfoUtils.getSubscriptionType());
            PullConsumerLocalInfoUtils.removeSubscriptionType();

            // When the subscription method is assign, set the message queue and topic of the wrapper, and set the
            // topic of the wrapper for non-assign methods
            if (pullConsumerWrapper.getSubscriptionType().equals(SubscriptionType.ASSIGN)) {
                updateAssignWrapperInfo(pullConsumerWrapper);
            } else {
                pullConsumerWrapper.setSubscribedTopics(
                        pullConsumerWrapper.getRebalanceImpl().getSubscriptionInner().keySet());
            }
        }
    }

    private void updateAssignWrapperInfo(DefaultLitePullConsumerWrapper pullConsumerWrapper) {
        Collection<MessageQueue> messageQueue = PullConsumerLocalInfoUtils.getMessageQueue();
        pullConsumerWrapper.setMessageQueues(messageQueue);
        pullConsumerWrapper.setSubscribedTopics(getMessageQueueTopics(messageQueue));
        PullConsumerLocalInfoUtils.removeMessageQueue();
    }
}
