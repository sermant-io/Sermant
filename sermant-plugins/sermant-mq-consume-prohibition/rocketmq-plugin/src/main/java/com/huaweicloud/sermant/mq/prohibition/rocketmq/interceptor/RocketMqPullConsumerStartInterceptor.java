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

import org.apache.rocketmq.client.consumer.DefaultLitePullConsumer;
import org.apache.rocketmq.common.message.MessageQueue;

import java.util.Collection;

/**
 * RocketMq pullConsumer启动拦截器
 *
 * @author daizhenyu
 * @since 2023-12-04
 **/
public class RocketMqPullConsumerStartInterceptor extends AbstractPullConsumerInterceptor {
    /**
     * 无参构造方法
     */
    public RocketMqPullConsumerStartInterceptor() {
    }

    /**
     * 有参构造方法
     *
     * @param handler 拦截点处理器
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

        // 获取存入缓存的消费者包装类实例
        DefaultLitePullConsumerWrapper pullConsumerWrapper =
                RocketMqPullConsumerController.getPullConsumerWrapper(pullConsumer);
        updatePushConsumerWrapperInfo(pullConsumerWrapper);

        if (handler != null) {
            handler.doAfter(context);
            return context;
        }

        // 消费者启动会根据缓存的禁消费配置对消费者执行禁消费
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

            // 订阅方式为assign时设置wrapper的消息队列和topic，非assign方式设置wrapper的topic
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