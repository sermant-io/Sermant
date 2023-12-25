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

package com.huaweicloud.sermant.mq.prohibition.rocketmq.interceptor;

import com.huaweicloud.sermant.core.plugin.agent.entity.ExecuteContext;
import com.huaweicloud.sermant.mq.prohibition.rocketmq.utils.PullConsumerLocalInfoUtils;
import com.huaweicloud.sermant.rocketmq.constant.SubscriptionType;
import com.huaweicloud.sermant.rocketmq.controller.RocketMqPullConsumerController;
import com.huaweicloud.sermant.rocketmq.extension.RocketMqConsumerHandler;
import com.huaweicloud.sermant.rocketmq.wrapper.DefaultLitePullConsumerWrapper;
import com.huaweicloud.sermant.utils.InvokeUtils;

import org.apache.rocketmq.client.consumer.DefaultLitePullConsumer;
import org.apache.rocketmq.common.message.MessageQueue;

import java.util.Collection;

/**
 * RocketMq pullConsumer指定队列拦截器
 *
 * @author daizhenyu
 * @since 2023-12-15
 **/
public class RocketMqPullConsumerAssignInterceptor extends AbstractPullConsumerInterceptor {
    /**
     * 无参构造方法
     */
    public RocketMqPullConsumerAssignInterceptor() {
    }

    /**
     * 有参构造方法
     *
     * @param handler 处理器
     */
    public RocketMqPullConsumerAssignInterceptor(RocketMqConsumerHandler handler) {
        super(handler);
    }

    @Override
    public ExecuteContext before(ExecuteContext context) {
        if (InvokeUtils.isRocketMqInvokeBySermant(Thread.currentThread().getStackTrace())) {
            return context;
        }
        if (handler != null) {
            handler.doBefore(context);
        }
        return context;
    }

    @Override
    public ExecuteContext after(ExecuteContext context) {
        if (InvokeUtils.isRocketMqInvokeBySermant(Thread.currentThread().getStackTrace())) {
            return context;
        }
        DefaultLitePullConsumerWrapper wrapper = RocketMqPullConsumerController
                .getPullConsumerWrapper((DefaultLitePullConsumer)context.getObject());

        Object messageQueueObject = context.getArguments()[0];
        if (messageQueueObject == null || !(messageQueueObject instanceof Collection)) {
            return context;
        }
        Collection<MessageQueue> messageQueue = (Collection<MessageQueue>) messageQueueObject;

        if (wrapper == null) {
            setAssignLocalInfo(messageQueue);
        } else {
            updateAssignWrapperInfo(wrapper, messageQueue);
        }

        if (handler != null) {
            handler.doAfter(context);
            return context;
        }

        // 指定消费的队列后，需根据禁消费的topic配置对消费者开启或禁止消费
        disablePullConsumption(wrapper);
        return context;
    }

    @Override
    public ExecuteContext onThrow(ExecuteContext context) {
        if (InvokeUtils.isRocketMqInvokeBySermant(Thread.currentThread().getStackTrace())) {
            return context;
        }
        if (handler != null) {
            handler.doOnThrow(context);
        }
        return context;
    }

    private void updateAssignWrapperInfo(DefaultLitePullConsumerWrapper pullConsumerWrapper,
            Collection<MessageQueue> messageQueue) {
        pullConsumerWrapper.setMessageQueues(messageQueue);
        pullConsumerWrapper.setSubscribedTopics(getMessageQueueTopics(messageQueue));
        pullConsumerWrapper.setSubscriptionType(SubscriptionType.ASSIGN);
    }

    private void setAssignLocalInfo(Collection<MessageQueue> messageQueue) {
        PullConsumerLocalInfoUtils.setSubscriptionType(SubscriptionType.ASSIGN);
        PullConsumerLocalInfoUtils.setMessageQueue(messageQueue);
    }
}
