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

package com.huaweicloud.sermant.mq.prohibition.rocketmq.utils;

import com.huaweicloud.sermant.rocketmq.constant.SubscriptionType;

import org.apache.rocketmq.common.message.MessageQueue;

import java.util.ArrayList;
import java.util.Collection;

/**
 * pull消费者的线程变量
 *
 * @author daizhenyu
 * @since 2023-12-20
 **/
public class PullConsumerLocalInfoUtils {
    private static ThreadLocal<SubscriptionType> subscriptionTypeThreadLocal =
            new ThreadLocal<>();

    private static ThreadLocal<Collection<MessageQueue>> messageQueueThreadLocal =
            new ThreadLocal<>();

    private PullConsumerLocalInfoUtils() {
    }

    /**
     * 获取消费者的订阅类型
     *
     * @return 订阅类型
     */
    public static SubscriptionType getSubscriptionType() {
        SubscriptionType subscriptionType = subscriptionTypeThreadLocal.get();
        if (subscriptionType == null) {
            subscriptionType = SubscriptionType.NONE;
            subscriptionTypeThreadLocal.set(subscriptionType);
        }
        return subscriptionType;
    }

    /**
     * 设置消费者的订阅类型
     *
     * @param subscriptionType 订阅类型
     */
    public static void setSubscriptionType(SubscriptionType subscriptionType) {
        subscriptionTypeThreadLocal.set(subscriptionType);
    }

    /**
     * 移除消费者的订阅类型
     */
    public static void removeSubscriptionType() {
        subscriptionTypeThreadLocal.remove();
    }

    /**
     * 获取消费者assign的消费队列
     *
     * @return 消费者assign的消费队列
     */
    public static Collection<MessageQueue> getMessageQueue() {
        Collection<MessageQueue> messageQueues = messageQueueThreadLocal.get();
        if (messageQueues == null) {
            messageQueues = new ArrayList<>();
            messageQueueThreadLocal.set(messageQueues);
        }
        return messageQueues;
    }

    /**
     * 设置消费者assign的消费队列
     *
     * @param messageQueues 消费者assign的消费队列
     */
    public static void setMessageQueue(Collection<MessageQueue> messageQueues) {
        messageQueueThreadLocal.set(messageQueues);
    }

    /**
     * 移除消费者assign的消费队列
     */
    public static void removeMessageQueue() {
        messageQueueThreadLocal.remove();
    }
}
